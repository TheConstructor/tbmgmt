package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.UserDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationVariableValuesDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator.PermutationIterable;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps.ActionExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps.NodePreparator;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps.OutputEvaluator;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.SFTPClientOperation;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.SSHSessionOperation;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.SSHSessionOperationExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.CloseableProxy;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.SSHClientUtils;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by matthias on 13.11.15.
 */
@Configurable
public class ExperimentExecutor implements Callable<ExperimentState>, SSHSessionOperationExecutor {

    private static final Logger LOG = Logger.getLogger(ExperimentExecutor.class.getName());
    private static final Object NODE_RESERVATION_LOCK = new Object();
    public static final int AWAIT_TERMINATION_RETRIES = 5;

    private final Long experimentId;
    private final Instant startTime;
    private boolean experimentStarted = false;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private ExperimentReplicationVariableValuesDao experimentReplicationVariableValuesDao;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @Autowired
    private ExperimentControlConfiguration experimentControlConfiguration;

    @Autowired
    private ExperimentReplicationResultDao experimentReplicationResultDao;

    @Autowired
    private UserDao userDao;

    private ScheduledFuture<ExperimentState> future;

    public ExperimentExecutor(final Experiment experiment) {
        this.experimentId = experiment.getId();
        startTime = experiment.getStartTime();
    }

    /**
     * Collection of SSHClients to remove delay when executing actions on clients.
     */
    private final ConcurrentHashMap<Long, Queue<SSHClient>> sshClients = new ConcurrentHashMap<>();

    private Queue<SSHClient> getSSHClientsQueue(final Node node) {
        return sshClients.computeIfAbsent(node.getId(), k -> new LinkedBlockingQueue<>());
    }

    private SSHClient getSSHClient(final Node node, final String username) throws IOException, InterruptedException {
        if (experimentControlConfiguration.isReuseSSHConnections()) {
            SSHClient sshClient;
            while ((sshClient = getSSHClientsQueue(node).poll()) != null) {
                if (sshClient.isConnected()) {
                    return sshClient;
                } else {
                    try {
                        sshClient.close();
                    } catch (final InterruptedIOException e) {
                        final InterruptedException ie = new InterruptedException();
                        ie.initCause(e);
                        throw ie;
                    } catch (final IOException e) {
                        LOG.log(Level.INFO, "Error closing SSH-Connection: " + e, e);
                    }
                }
            }
        }
        return SSHClientUtils.clientFor(node, resourcePatternResolver, username);
    }

    private void returnSSHClient(final Node node, final SSHClient sshClient) throws IOException {
        if (!experimentControlConfiguration.isReuseSSHConnections() || !sshClient.isConnected() || !getSSHClientsQueue(
                node).offer(sshClient)) {
            sshClient.close();
        }
    }

    public void closeSSHConnections(final ExperimentLogger log) {
        for (final Queue<SSHClient> sshClientQueue : sshClients.values()) {
            SSHClient sshClient;
            while ((sshClient = sshClientQueue.poll()) != null) {
                try {
                    sshClient.close();
                } catch (final IOException e) {
                    log.log(Level.INFO, "Error closing SSH-Connection: " + e, e);
                }
            }
        }
    }

    @Override
    public <R> R executeOn(final Node node, final String username, final SSHSessionOperation<R> operation)
            throws IOException, InterruptedException {
        try (final CloseableProxy<SSHClient> proxy = new CloseableProxy<>(getSSHClient(node, username),
                c -> returnSSHClient(node, c))) {
            final R result;
            try (final Session session = proxy.get().startSession()) {
                result = operation.execute(session);
            }
            proxy.setReusable();
            return result;
        }
    }

    public <R> R sftpConnectionTo(final Node node, final String username, final SFTPClientOperation<R> operation)
            throws IOException, InterruptedException {
        try (final CloseableProxy<SSHClient> proxy = new CloseableProxy<>(getSSHClient(node, username),
                c -> returnSSHClient(node, c))) {
            final R result;
            try (final SFTPClient sftpClient = proxy.get().newSFTPClient()) {
                result = operation.execute(sftpClient);
            }
            proxy.setReusable();
            return result;
        }
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public boolean isExperimentStarted() {
        synchronized (this) {
            return experimentStarted;
        }
    }

    @Override
    public ExperimentState call() {
        return readOnlyTransactionTemplate.execute((transactionStatus -> {
            final Experiment experiment = experimentDao.find(experimentId);
            if (experiment == null) {
                LOG.log(Level.SEVERE, "Could not load experiment by id {}, although it once existed", experimentId);
                return ExperimentState.FAILED;
            }
            final ExperimentLogger log = new ExperimentLogger(LOG, experiment);

            // synchronized so we avoid the problem, where the ExperimentMonitorImpl queries experimentStarted just
            // before it is set to true
            synchronized (ExperimentExecutor.this) {
                if (isFutureCanceled() || !Objects.equals(startTime, experiment.getStartTime())) {
                    return experiment.getState();
                }
                experimentStarted = true;
            }
            try {
                final User user = experiment.getCreatorId() == null ? null : userDao.find(experiment.getCreatorId());
                if (experiment.isInteractive() && (user == null || StringUtils.isBlank(user.getUserName()))) {
                    log.log(Level.SEVERE,
                            "Could not find user with id " + experiment.getCreatorId() + " for interactive experiment "
                                    + experimentId);
                    setStateWithoutFailing(experiment, ExperimentState.FAILED);
                    return ExperimentState.FAILED;
                }

                setStateOrFail(experiment, ExperimentState.WAITING_FOR_NODES);

                waitForLockedNodes(log, experiment);
                ensureFutureNotCanceled();
                log.log(Level.INFO, "Acquired all nodes.");

                final Map<Long, Node> usedNodes = experiment.getUsedNodes();
                final List<Node> nodes = new ArrayList<>(usedNodes.values());
                final ExecutorService executorService = Executors.newFixedThreadPool(nodes.size());
                try {
                    final String automationUsername = experimentControlConfiguration.getAutomationUsername();
                    final String username = experiment.isInteractive() ? user.getUserName() : automationUsername;

                    // 1. Verify node configuration
                    final NodePreparator nodePreparator = new NodePreparator(this);
                    final List<Node> changedNodes = nodePreparator.verifyNodeConfiguration(experiment, nodes, username);

                    // 2. Restart nodes?
                    if (experiment.isRestartNodes()) {
                        ensureFutureNotCanceled();
                        log.log(Level.INFO, "Restarting all nodes.");
                        nodePreparator.restartNodes(log, experiment, nodes, executorService, automationUsername);
                    } else if (!changedNodes.isEmpty()) {
                        ensureFutureNotCanceled();
                        log.log(Level.INFO, "Restarting nodes " + changedNodes
                                .stream()
                                .map(Node::getName)
                                .collect(Collectors.joining(", ")) + " as their boot-configuration changed");
                        nodePreparator.restartNodes(log, experiment, changedNodes, executorService, automationUsername);
                    }

                    ensureFutureNotCanceled();
                    // All nodes responding?
                    log.log(Level.INFO, "Checking if all nodes respond to SSH-connections");
                    nodePreparator.checkNodesResponding(log, nodes, executorService, automationUsername);
                    log.log(Level.INFO, "All nodes are responding.");

                    // Interactive?
                    if (!experiment.isInteractive()) {
                        ensureFutureNotCanceled();
                        // 3. Create directory structure
                        final ActionExecutor actionExecutor = new ActionExecutor(this);
                        log.log(Level.INFO, "Creating directories.");
                        actionExecutor.createDirectories(log, experiment, nodes, executorService, automationUsername);

                        // 4. Copy files to nodes
                        ensureFutureNotCanceled();
                        log.log(Level.INFO, "Copying Files to nodes.");
                        actionExecutor.copyFilesToNodes(experiment, nodes, executorService, automationUsername);

                        ensureFutureNotCanceled();

                        for (long replication = 0; replication < experiment.getReplications(); replication++) {
                            if (replication != 0) {
                                log.log(Level.INFO, "Waiting " + experiment.getPauseBetweenReplications()
                                        + " before starting replication " + replication);
                                Thread.sleep(experiment.getPauseBetweenReplications().toMillis());
                            }
                            log.log(Level.INFO, "Starting replication " + replication);

                            final ExperimentReplicationResult replicationResult = new ExperimentReplicationResult();
                            replicationResult.setSequence(replication);
                            replicationResult.setExperiment(experiment);
                            experimentReplicationResultDao.persistWithSideTransaction(replicationResult);

                            final ExperimentLogger replicationLog = log.withReplicationResult(replicationResult);

                            final LinkedHashMap<String, ExperimentVariable> nameToVariable = experiment
                                    .getVariables()
                                    .stream()
                                    .collect(Collectors.toMap(ExperimentVariable::getName, UnaryOperator.identity(),
                                            (u, v) -> {
                                                throw new IllegalStateException(
                                                        String.format("Two variables with the same name: %s and %s", u,
                                                                v));
                                            }, LinkedHashMap::new));
                            final PermutationIterable<String, String> variablePermutations =
                                    new PermutationIterable<>(nameToVariable);
                            long sequence = 0;
                            for (final Map<String, String> variablePermutation : variablePermutations) {
                                final ExperimentReplicationVariableValues variableValues =
                                        new ExperimentReplicationVariableValues();
                                variableValues.setExperimentReplicationResult(replicationResult);
                                variableValues.setSequence(sequence++);
                                variableValues.setVariableValues(variablePermutation);
                                experimentReplicationVariableValuesDao.persistWithSideTransaction(variableValues);

                                final ExperimentLogger variableValuesLog =
                                        replicationLog.withVariableValues(variableValues);

                                // 5. Execute some actions...
                                setStateOrFail(experiment, ExperimentState.RUNNING);
                                log.log(Level.INFO, "Executing actions");
                                actionExecutor.executeActions(variableValuesLog, experiment, variableValues,
                                        automationUsername);

                                // 6. Evaluate
                                setStateOrFail(experiment, ExperimentState.EVALUATING);
                                log.log(Level.INFO, "Evaluating action output");
                                final OutputEvaluator outputEvaluator = new OutputEvaluator(this);
                                outputEvaluator.evaluateOutput(experiment, nodes, executorService, replicationResult,
                                        variableValues, automationUsername);
                            }
                        }

                        // 7. Clean Up
                        setStateOrFail(experiment, ExperimentState.CLEANING_UP);
                        log.log(Level.INFO, "Cleaning up.");
                        actionExecutor.deleteDirectories(log, experiment, nodes, executorService, automationUsername);
                    } else {
                        // TODO Cluster-SSH-Script
                        setStateOrFail(experiment, ExperimentState.RUNNING);
                        log.log(Level.INFO, "You can now use the nodes until " + experiment.getEndTime());
                        while (Instant.now().isBefore(experiment.getEndTime())) {
                            Thread.sleep(1000);
                            ensureFutureNotCanceled();
                        }
                    }
                } finally {
                    executorService.shutdown();
                    for (int i = 0; i <= AWAIT_TERMINATION_RETRIES; i++) {
                        try {
                            executorService.awaitTermination(1, TimeUnit.DAYS);
                            break;
                        } catch (final InterruptedException e) {
                            log.log(Level.WARNING,
                                    "Got interrupted waiting for executorService to terminate. Will retry " +
                                            (AWAIT_TERMINATION_RETRIES - i) + " times");
                        }
                    }
                }

                // This time we can ignore cancellation - we are already done.
                setStateWithoutFailing(experiment, ExperimentState.SUCCEEDED);
            } catch (ExperimentCancelledException | InterruptedException e) {
                log.log(Level.INFO, "Cancelling experiment " + experimentId + " due to cancellation request", e);
                setStateWithoutFailing(experiment, ExperimentState.FAILED);
            } catch (final Exception e) {
                log.log(Level.WARNING, "Cancelling experiment " + experimentId + " due to exception: " + e, e);
                setStateWithoutFailing(experiment, ExperimentState.FAILED);
            } finally {
                closeSSHConnections(log);
                experimentDao.immediatelySetStateIfNotEnded(experiment, ExperimentState.FAILED);
            }
            return experiment.getState();
        }));
    }

    public boolean isFutureCanceled() {
        return future != null && future.isCancelled();
    }

    public void ensureFutureNotCanceled() throws ExperimentCancelledException {
        if (isFutureCanceled()) {
            throw new ExperimentCancelledException("Experiment canceled by cancelling future.");
        }
    }

    public void setStateOrFail(final Experiment experiment, final ExperimentState state)
            throws ExperimentCancelledException {
        ensureFutureNotCanceled();
        final ExperimentState previousState = setStateWithoutFailing(experiment, state);
        if (ExperimentState.CANCELATION_REQUESTED.equals(previousState) || ExperimentState.FAILED.equals(
                previousState)) {
            throw new ExperimentCancelledException("Experiment canceled by database trigger.");
        }
    }

    public ExperimentState setStateWithoutFailing(final Experiment experiment, final ExperimentState state) {
        return experimentDao.immediatelySetState(experiment, state);
    }

    protected void waitForLockedNodes(final ExperimentLogger log, final Experiment experiment)
            throws InterruptedException, ExperimentCancelledException {
        do {
            final List<Experiment> conflictingExperiments;
            // global lock to ensure that only one experiments siwtches to a blocking state if multiple experiments
            // need the same nodes
            synchronized (NODE_RESERVATION_LOCK) {
                conflictingExperiments = experimentDao.getConflictingExperiments(experiment);
                if (conflictingExperiments.isEmpty()) {
                    setStateOrFail(experiment, ExperimentState.PREPARING);
                    return;
                }
            }
            log.log(Level.INFO, () -> String.format(
                    "Waiting with the execution of experiment %d until the experiment(s) %s have finished.",
                    experimentId, conflictingExperiments
                            .stream()
                            .map(Experiment::getId)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))));
            Thread.sleep(30000);
            ensureFutureNotCanceled();
        } while (true);
    }

    public void setFuture(final ScheduledFuture<ExperimentState> future) {
        this.future = future;
    }

    public ExperimentControlConfiguration getExperimentControlConfiguration() {
        return experimentControlConfiguration;
    }

    private static class ExperimentCancelledException extends Exception {
        public ExperimentCancelledException(final String message) {
            super(message);
        }
    }
}
