package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentActionExecutionDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentEvaluationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogEntryCreator;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.VariablesUtil;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.SSHClientUtils;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.TaskResultHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Created by matthias on 26.02.16.
 */
@Configurable
public class OutputEvaluator {

    private final ExperimentExecutor experimentExecutor;

    @Autowired
    private ExperimentEvaluationResultDao experimentEvaluationResultDao;

    @Autowired
    private ExperimentActionExecutionDao experimentActionExecutionDao;

    @Autowired
    private ActionResultStorageHelper actionResultStorageHelper;

    public OutputEvaluator(final ExperimentExecutor experimentExecutor) {
        this.experimentExecutor = experimentExecutor;
    }

    public void evaluateOutput(final Experiment experiment, final List<Node> nodes,
                               final ExecutorService executorService,
                               final ExperimentReplicationResult replicationResult,
                               final ExperimentReplicationVariableValues variableValues, final String username)
            throws ExecutionException, InterruptedException {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final ExperimentActionBlock actionBlock : experiment.getActionBlocks()) {
            for (final ExperimentAction action : actionBlock.getActions()) {
                final String evaluationFileName;
                if (action.getEvaluationFile() != null) {
                    evaluationFileName = action.getEvaluationFile().getFileName();
                } else if (action.getEvaluationScript() != null) {
                    evaluationFileName = action.getEvaluationScript().getFileName();
                } else {
                    evaluationFileName = null;
                }
                final String baseEvaluationParameter = VariablesUtil.replaceVariables(action.getEvaluationParameter(),
                        variableValues.getVariableValues());

                final long replication = replicationResult.getSequence();
                final TaskResultHelper<Integer> taskResultHelper = new TaskResultHelper<>(executorService);
                for (final Node node : nodes) {
                    final List<ExperimentActionExecution> actionExecutions =
                            experimentActionExecutionDao.getFor(variableValues, action, node);

                    // taskResultHelper has one thread for each node participating in the experiment and we don't want
                    // to run two evaluations on the same node in parallel -> submit inner block to taskResultHelper
                    taskResultHelper.submit(() -> {
                        for (final ExperimentActionExecution actionExecution : actionExecutions) {
                            final LogEntryCreator logEntryCreator =
                                    actionResultStorageHelper.getLogEntryCreator(experiment, action, replicationResult,
                                            variableValues, actionExecution, node);
                            final LogEntryCreator evaluationResultCreator = (logLevel, line) -> {
                                try {
                                    final JsonNode jsonNode = objectMapper.readTree(line);
                                    final ExperimentEvaluationResult evaluationResult =
                                            new ExperimentEvaluationResult();
                                    evaluationResult.setActionExecution(actionExecution);
                                    evaluationResult.setData(jsonNode);
                                    actionResultStorageHelper.persistAndWait(experimentEvaluationResultDao,
                                            evaluationResult, "Could not store evaluation-result");
                                } catch (final IOException e) {
                                    logEntryCreator.createLogEntry(LogEntryCreator.LogReason.WARNING,
                                            "Invalid JSON: " + line);
                                } catch (final InterruptedException e) {
                                    throw new IllegalStateException(e);
                                }
                            };

                            final String replicationDirectory =
                                    getExperimentControlConfiguration().getReplicationDirectory(experiment, node,
                                            replication);

                            final String commandOutputFileName =
                                    getExperimentControlConfiguration().getCommandOutputFileName(action, replication,
                                            variableValues, actionExecution);

                            if (evaluationFileName != null) {
                                //noinspection StringBufferReplaceableByString
                                final StringBuilder sb = new StringBuilder();
                                // cd to replication dir;
                                sb.append("cd ").append(replicationDirectory).append("; ");
                                // script runner + script+name
                                sb
                                        .append(getExperimentControlConfiguration().getEvaluationScriptRunner())
                                        .append(" ")
                                        .append(getExperimentControlConfiguration().getReplicationToNodeDirectory(
                                                experiment, node))
                                        .append(evaluationFileName);

                                // + optional parameter
                                if (StringUtils.isNotBlank(baseEvaluationParameter)) {
                                    final String withAddresses = VariablesUtil.replaceVariables(baseEvaluationParameter,
                                            actionExecution.getNodeAddressValues());
                                    final String withReturnCode = VariablesUtil.replaceVariables(withAddresses,
                                            Collections.singletonMap("returnCode",
                                                    String.valueOf(actionExecution.getReturnCode())));
                                    sb.append(" ").append(withReturnCode);
                                }

                                // redirect stdout of action into stdin
                                sb.append(" < ").append(commandOutputFileName);

                                final String commandline = sb.toString();
                                experimentExecutor.executeOn(node, username,
                                        session -> SSHClientUtils.executeCommand(session, commandline, null,
                                                logEntryCreator, evaluationResultCreator));
                            }

                            experimentExecutor.sftpConnectionTo(node, username, session -> {
                                session.rm(replicationDirectory + '/' + commandOutputFileName);
                                return null;
                            });
                        }
                        return actionExecutions.size();
                    });
                }
                taskResultHelper.collectOrThrowExceptions();
            }
        }
    }

    public ExperimentControlConfiguration getExperimentControlConfiguration() {
        return experimentExecutor.getExperimentControlConfiguration();
    }
}
