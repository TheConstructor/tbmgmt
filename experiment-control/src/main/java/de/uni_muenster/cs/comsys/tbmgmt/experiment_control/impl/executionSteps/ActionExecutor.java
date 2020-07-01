package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps;

import com.google.common.math.IntMath;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExecutionMode;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.AbstractExecutableActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.ActionBlockExecutionState;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.ExecutableParallelActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.ExecutableSerialActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.ExecutableServerActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions.ExecutableTimedActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExecutableActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.TaskResultHelper;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.PathHelper;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by matthias on 26.02.16.
 */
@Configurable
public class ActionExecutor {

    private final ExperimentExecutor experimentExecutor;

    public ActionExecutor(final ExperimentExecutor experimentExecutor) {
        this.experimentExecutor = experimentExecutor;
    }

    public void createDirectories(final ExperimentLogger log, final Experiment experiment, final List<Node> nodes,
                                  final ExecutorService executorService, final String username)
            throws ExecutionException, InterruptedException {
        final Object lock = new Object();
        final TaskResultHelper<Void> taskResultHelper = new TaskResultHelper<>(executorService);
        for (final Node node : nodes) {
            final ExperimentLogger nodeLog = log.withNode(node);
            taskResultHelper.submit(() -> experimentExecutor.sftpConnectionTo(node, username, sftpClient -> {
                // One node at a time as for Net-Boot we might encounter race-conditions
                synchronized (lock) {
                    final String experimentDir = experimentExecutor
                            .getExperimentControlConfiguration()
                            .getExperimentDirectory(experiment, node);
                    createDirectoryIfNotPresent(nodeLog, sftpClient, experimentDir);

                    final String nodeDir =
                            experimentExecutor.getExperimentControlConfiguration().getNodeDirectory(experiment, node);
                    createDirectoryIfNotPresent(nodeLog, sftpClient, nodeDir);
                }

                // All nodes at once, as there should by contract never be colliding replication-directories between
                // nodes and we should have created parent-directory by now
                for (long replication = 0; replication < experiment.getReplications(); replication++) {
                    final String replicationDir = experimentExecutor
                            .getExperimentControlConfiguration()
                            .getReplicationDirectory(experiment, node, replication);
                    createDirectoryIfNotPresent(nodeLog, sftpClient, replicationDir);
                }
                return null;
            }));
        }
        taskResultHelper.collectOrThrowExceptions();
    }

    public void copyFilesToNodes(final Experiment experiment, final List<Node> nodes,
                                 final ExecutorService executorService, final String username)
            throws ExecutionException, InterruptedException {
        if (!experiment.getFiles().isEmpty()) {
            final TaskResultHelper<Integer> taskResultHelper = new TaskResultHelper<>(executorService);
            for (final Node node : nodes) {
                taskResultHelper.submit(() -> experimentExecutor.sftpConnectionTo(node, username, sftpClient -> {
                    //noinspection resource
                    final PathHelper pathHelper = sftpClient.getSFTPEngine().getPathHelper();

                    final String nodeDir =
                            experimentExecutor.getExperimentControlConfiguration().getNodeDirectory(experiment, node);
                    for (final ExperimentFile file : experiment.getFiles()) {
                        if (file.getFile() == null || !file.getFile().exists()) {
                            throw new IllegalStateException("Experiment has an entry for file " + file.getFileName()
                                    + ", but it does not exist on disk");
                        }
                        final String filePath = nodeDir + pathHelper.getPathSeparator() + file.getFileName();
                        sftpClient.put(new FileSystemFile(file.getFile()), filePath);
                    }
                    return null;
                }));
            }
            taskResultHelper.collectOrThrowExceptions();
        }

        final Map<Node, Set<EvaluationScript>> evaluationScripts = new HashMap<>();
        experiment
                .getActionBlocks()
                .stream()
                .flatMap(ab -> ab.getActions().stream().filter(a -> a.getEvaluationScript() != null))
                .forEach(a -> a
                        .getTargetedNodeGroup()
                        .getNodes()
                        .stream()
                        .forEach(n -> evaluationScripts
                                .computeIfAbsent(n, x -> new HashSet<>())
                                .add(a.getEvaluationScript())));
        if (!evaluationScripts.isEmpty()) {
            final TaskResultHelper<Integer> taskResultHelper = new TaskResultHelper<>(executorService);
            for (final Map.Entry<Node, Set<EvaluationScript>> entry : evaluationScripts.entrySet()) {
                final Node node = entry.getKey();
                taskResultHelper.submit(() -> experimentExecutor.sftpConnectionTo(node, username, sftpClient -> {
                    //noinspection resource
                    final PathHelper pathHelper = sftpClient.getSFTPEngine().getPathHelper();

                    final String nodeDir =
                            experimentExecutor.getExperimentControlConfiguration().getNodeDirectory(experiment, node);
                    for (final EvaluationScript evaluationScript : entry.getValue()) {
                        final String filePath =
                                nodeDir + pathHelper.getPathSeparator() + evaluationScript.getFileName();
                        sftpClient.put(new FileSystemFile(evaluationScript.getFile()), filePath);
                    }
                    return null;
                }));
            }
            taskResultHelper.collectOrThrowExceptions();
        }
    }

    public void executeActions(final ExperimentLogger log, final Experiment experiment,
                               final ExperimentReplicationVariableValues variableValues, final String username)
            throws InterruptedException {
        final List<ExecutableActionBlock> regularBlocks = new ArrayList<>();
        final List<ExecutableServerActionBlock> serverBlocks = new ArrayList<>();

        for (final ExperimentActionBlock actionBlock : experiment.getActionBlocks()) {
            if (ExecutionMode.SERVER.equals(actionBlock.getExecutionMode())) {
                serverBlocks.add(new ExecutableServerActionBlock(experimentExecutor, actionBlock));
            } else {
                regularBlocks.add(asExecutableActionBlock(actionBlock));
            }
        }

        if (regularBlocks.isEmpty()) {
            throw new IllegalStateException(
                    "Experiment needs at least one non-server block (which will be run before any server-block)");
        }
        final ExecutableActionBlock initBlock = regularBlocks.remove(0);

        final int parallelTasks = Math.max(initBlock.requiredParallelTasks(),
                regularBlocks.stream().mapToInt(ExecutableActionBlock::requiredParallelTasks).max().orElse(0)
                        + serverBlocks
                        .stream()
                        .mapToInt(ExecutableActionBlock::requiredParallelTasks)
                        .reduce(0, IntMath::checkedAdd));

        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(parallelTasks + 2);
        try {
            final Instant startOfIteration = Instant.now();

            initBlock.execute(log, executorService, startOfIteration, variableValues, username);

            final List<ActionBlockExecutionState> serverStates = new ArrayList<>(serverBlocks.size());
            InterruptedException ie = null;
            try {
                for (final ExecutableServerActionBlock serverBlock : serverBlocks) {
                    serverStates.add(
                            serverBlock.execute(log, executorService, startOfIteration, variableValues, username));
                }

                for (final ExecutableActionBlock regularBlock : regularBlocks) {
                    regularBlock.execute(log, executorService, startOfIteration, variableValues, username);
                }

                // Work is done - terminate actions in server-type-blocks
                // We first set terminated everywhere to reduce overall waiting with multiple server-blocks
                for (final ActionBlockExecutionState state : serverStates) {
                    state.getTerminated().set(true);
                }

                for (final ActionBlockExecutionState state : serverStates) {
                    AbstractExecutableActionBlock.awaitEndOfExecution(state);
                }
            } finally {
                for (final ActionBlockExecutionState state : serverStates) {
                    state.getTerminated().set(true);
                }

                for (final ActionBlockExecutionState state : serverStates) {
                    try {
                        AbstractExecutableActionBlock.cancelAndWait(state);
                    } catch (final InterruptedException e) {
                        log.log(Level.WARNING, "Got interrupted waiting for a server-block to finish", e);
                        if (ie == null) {
                            ie = e;
                        } else {
                            ie.addSuppressed(e);
                        }
                    }
                }
            }
            if (ie != null) {
                throw ie;
            }
        } finally {
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.DAYS);
        }
    }

    public void deleteDirectories(final ExperimentLogger log, final Experiment experiment, final List<Node> nodes,
                                  final ExecutorService executorService, final String username)
            throws ExecutionException, InterruptedException {
        final int nodeCount = nodes.size();
        final Semaphore semaphore = new Semaphore(nodeCount);
        final TaskResultHelper<Void> taskResultHelper = new TaskResultHelper<>(executorService);
        for (final Node node : nodes) {
            final ExperimentLogger nodeLog = log.withNode(node);
            taskResultHelper.submit(() -> experimentExecutor.sftpConnectionTo(node, username, sftpClient -> {
                // All nodes at once, as there should by contract never be colliding replication-directories between
                // nodes
                semaphore.acquire();
                try {
                    for (long replication = 0; replication < experiment.getReplications(); replication++) {
                        final String replicationDir = experimentExecutor
                                .getExperimentControlConfiguration()
                                .getReplicationDirectory(experiment, node, replication);
                        deleteDir(nodeLog, sftpClient, replicationDir);
                    }
                } finally {
                    semaphore.release();
                }

                // One node at a time as for Net-Boot we might encounter race-conditions
                semaphore.acquire(nodeCount);
                try {
                    final String nodeDir =
                            experimentExecutor.getExperimentControlConfiguration().getNodeDirectory(experiment, node);
                    deleteDir(nodeLog, sftpClient, nodeDir);

                    final String experimentDir = experimentExecutor
                            .getExperimentControlConfiguration()
                            .getExperimentDirectory(experiment, node);
                    deleteDir(nodeLog, sftpClient, experimentDir);
                } finally {
                    semaphore.release(nodeCount);
                }
                return null;
            }));
        }
        taskResultHelper.collectOrThrowExceptions();
    }

    protected static void deleteDir(final ExperimentLogger log, final SFTPClient sftpClient, final String dirPath)
            throws IOException {
        if (sftpClient.statExistence(dirPath) == null) {
            return;
        }
        final List<RemoteResourceInfo> remoteResourceInfos;
        try {
            remoteResourceInfos = sftpClient.ls(dirPath);
        } catch (final IOException e) {
            if (sftpClient.statExistence(dirPath) == null) {
                log.log(Level.FINE, "Could not delete \"" + dirPath + "\" as it seems it was already deleted", e);
            } else {
                log.log(Level.WARNING, "Could not delete \"" + dirPath + "\": " + e.getMessage(), e);
            }
            return;
        }
        for (final RemoteResourceInfo remoteResourceInfo : remoteResourceInfos) {
            // These paths are filtered in ls-call, but this should never hurt.
            if (".".equals(remoteResourceInfo.getName()) || "..".equals(remoteResourceInfo.getName())) {
                continue;
            }
            final String childPath = remoteResourceInfo.getPath();
            if (remoteResourceInfo.isDirectory()) {
                deleteDir(log, sftpClient, childPath);
            } else {
                try {
                    sftpClient.rm(childPath);
                } catch (final IOException e) {
                    if (sftpClient.statExistence(childPath) == null) {
                        log.log(Level.FINE, "Could not delete \"" + childPath + "\" as it seems it was already deleted",
                                e);
                    } else {
                        log.log(Level.WARNING, "Could not delete \"" + childPath + "\": " + e.getMessage(), e);
                    }
                }
            }
        }
        try {
            sftpClient.rmdir(dirPath);
        } catch (final IOException e) {
            if (sftpClient.statExistence(dirPath) == null) {
                log.log(Level.FINE, "Could not delete \"" + dirPath + "\" as it seems it was already deleted", e);
            } else {
                log.log(Level.WARNING, "Could not delete \"" + dirPath + "\": " + e.getMessage(), e);
            }
        }
    }

    private ExecutableActionBlock asExecutableActionBlock(final ExperimentActionBlock actionBlock) {
        switch (actionBlock.getExecutionMode()) {
            case SERIAL:
                return new ExecutableSerialActionBlock(experimentExecutor, actionBlock);
            case PARALLEL:
                return new ExecutableParallelActionBlock(experimentExecutor, actionBlock);
            case TIMED:
                return new ExecutableTimedActionBlock(experimentExecutor, actionBlock);
            default:
                throw new IllegalArgumentException("Unexpected ExecutionMode " + actionBlock.getExecutionMode());
        }
    }

    private static void createDirectoryIfNotPresent(final ExperimentLogger log, final SFTPClient sftpClient,
                                                    final String path) throws IOException {
        final FileAttributes fileAttributes = sftpClient.statExistence(path);
        if (fileAttributes == null) {
            try {
                sftpClient.mkdirs(path.startsWith("/") ? path : "./" + path);
            } catch (final IOException e) {
                // we just double-check this did not fail, because the directory already existed
                final FileAttributes fileAttributes2 = sftpClient.statExistence(path);
                if (fileAttributes2 == null) {
                    throw new IOException("Error trying to create " + path, e);
                } else {
                    try {
                        verifyCreatedDirectory(log, path, fileAttributes2);
                    } catch (final RuntimeException e1) {
                        final IOException ioException = new IOException("Error trying to create " + path, e);
                        ioException.addSuppressed(e1);
                        throw ioException;
                    }
                }
            }
        } else {
            verifyCreatedDirectory(log, path, fileAttributes);
        }
    }

    private static void verifyCreatedDirectory(final ExperimentLogger log, final String path,
                                               final FileAttributes fileAttributes) {
        final FileMode.Type type = fileAttributes.getType();
        if (type != null) {
            switch (type) {
                case BLOCK_SPECIAL:
                case CHAR_SPECIAL:
                case FIFO_SPECIAL:
                case SOCKET_SPECIAL:
                case REGULAR: {
                    final String message =
                            "Can not create a directory with the name of " + path + " as there is a file of type "
                                    + type;
                    log.log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }
                case DIRECTORY:
                    // That's what we want
                    break;
                case SYMLINK:
                case UNKNOWN:
                    log.log(Level.INFO, "There is an entry by the name of " + path + " and type " + type
                            + " hopefully it acts as directory");
                    break;
            }
        }
    }
}
