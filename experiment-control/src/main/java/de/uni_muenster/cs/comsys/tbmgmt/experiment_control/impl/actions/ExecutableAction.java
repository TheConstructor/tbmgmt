package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentActionExecutionDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogEntryCreator;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.SSHSessionOperationExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.SSHClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.dao.DataAccessException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 15.11.15.
 */
@Configurable
public class ExecutableAction {

    private static final Logger LOG = Logger.getLogger(ExecutableAction.class.getName());

    @Autowired
    private ExperimentControlConfiguration experimentControlConfiguration;

    @Autowired
    private ExperimentActionExecutionDao experimentActionExecutionDao;

    @Autowired
    private ActionResultStorageHelper actionResultStorageHelper;

    private final Semaphore before;
    private final Node node;
    private final String username;
    private final ExperimentAction action;
    private final ExperimentReplicationVariableValues variableValues;
    private final SSHSessionOperationExecutor sshOperationExecutor;
    private final CountDownLatch after;

    private final ExperimentReplicationResult replicationResult;
    private final Experiment experiment;

    public ExecutableAction(final Semaphore before, final Node node, final String username,
                            final ExperimentAction action, final ExperimentReplicationVariableValues variableValues,
                            final SSHSessionOperationExecutor sshOperationExecutor, final CountDownLatch after) {
        this.before = before;
        this.node = node;
        this.username = username;
        this.action = action;
        this.variableValues = variableValues;
        this.sshOperationExecutor = sshOperationExecutor;
        this.after = after;
        replicationResult = variableValues.getExperimentReplicationResult();
        experiment = replicationResult.getExperiment();
    }

    public List<ExperimentActionExecution> execute() throws IOException, InterruptedException {
        return execute(new AtomicBoolean(false));
    }

    public List<ExperimentActionExecution> execute(final AtomicBoolean terminated)
            throws IOException, InterruptedException {
        if (!before.tryAcquire()) {
            return Collections.emptyList();
        }
        try {
            final Duration actionDuration = action.getDuration();

            final Map<Map<String, String>, String> commands =
                    action.getCommands(variableValues.getVariableValues(), experiment::getNodeGroupByName);
            final List<ExperimentActionExecution> list = new ArrayList<>(commands.size());
            long sequence = 0;
            for (final Map.Entry<Map<String, String>, String> command : commands.entrySet()) {
                if (terminated.get()) {
                    return list;
                }
                final ExperimentActionExecution actionExecution = new ExperimentActionExecution();
                actionExecution.setVariableValues(variableValues);
                actionExecution.setAction(action);
                actionExecution.setNode(node);
                actionExecution.setSequence(sequence++);
                actionExecution.setNodeAddressValues(command.getKey());
                actionExecution.setStartedAfter(Instant.now());
                actionResultStorageHelper.persistAndWait(experimentActionExecutionDao, actionExecution,
                        "Error persisting invocation");
                list.add(actionExecution);

                final LogEntryCreator logEntryCreator =
                        actionResultStorageHelper.getLogEntryCreator(experiment, action, replicationResult,
                                variableValues, actionExecution, node);
                final Integer returnCode;
                try {
                    returnCode = sshOperationExecutor.executeOn(node, username, session -> {
                        if (experimentControlConfiguration.isUseCoreutilsTimeout() && actionDuration != null
                                && actionDuration.compareTo(Duration.ZERO) > 0) {
                            return SSHClientUtils.executeCommand(session,
                                    getCommandLine(command.getValue(), actionExecution, actionDuration), null,
                                    logEntryCreator, terminated);
                        } else {
                            return SSHClientUtils.executeCommand(session,
                                    getCommandLine(command.getValue(), actionExecution), actionDuration,
                                    logEntryCreator, terminated);
                        }
                    });
                } finally {
                    actionResultStorageHelper.submitAndWait(() -> {
                        try {
                            experimentActionExecutionDao.setEndedBefore(actionExecution, Instant.now());
                        } catch (final DataAccessException e) {
                            LOG.log(Level.WARNING, "Could not set ended-before", e);
                        }
                    }, "Could not set ended-before");
                }

                actionResultStorageHelper.submitAndWait(() -> {
                    try {
                        logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                                "Command finished with returnCode: " + returnCode);
                        experimentActionExecutionDao.setReturnCode(actionExecution, returnCode);
                    } catch (final DataAccessException e) {
                        LOG.log(Level.WARNING, "Could not store return code", e);
                    }
                }, "Could not set ended-before");
            }
            return list;
        } finally {
            after.countDown();
        }
    }

    private String getCommandLine(final String command, final ExperimentActionExecution actionExecution,
                                  final Duration duration) {
        // SIG_INT so e.g. ping writes it's statistics
        return getCd() + "timeout --preserve-status -s INT -k 10s " + duration.getSeconds() + "s " + getCommandPrefix()
                + getCommandAndRedirect(command, actionExecution);
    }

    private String getCommandLine(final String command, final ExperimentActionExecution actionExecution) {
        return getCd() +
                getCommandPrefix() + getCommandAndRedirect(command, actionExecution);
    }

    private String getCd() {
        return "cd " + experimentControlConfiguration.getReplicationDirectory(
                action.getExperimentActionBlock().getExperiment(), node, replicationResult.getSequence()) + "; ";
    }

    private String getCommandPrefix() {
        final String commandPrefix = experimentControlConfiguration.getCommandPrefix();
        if (StringUtils.isBlank(commandPrefix)) {
            return "";
        }
        return commandPrefix + " ";
    }

    private String getCommandAndRedirect(final String command, final ExperimentActionExecution actionExecution) {
        return command + " > " + experimentControlConfiguration.getCommandOutputFileName(action,
                replicationResult.getSequence(), variableValues, actionExecution);
    }
}
