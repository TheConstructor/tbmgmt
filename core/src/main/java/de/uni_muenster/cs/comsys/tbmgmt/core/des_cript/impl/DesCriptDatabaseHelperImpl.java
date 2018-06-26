package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentActionExecutionDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentEvaluationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationVariableValuesDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptDatabaseHelper;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.InvocationType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.IterationType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ObjectFactory;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ReplicationType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultActionBlockType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultActionType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultsType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.VariableValueType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.VariableValuesType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 05.03.16.
 */
public class DesCriptDatabaseHelperImpl implements DesCriptDatabaseHelper {
    private static final Logger LOG = Logger.getLogger(DesCriptDatabaseHelperImpl.class.getName());

    public static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ExperimentReplicationResultDao replicationResultDao;

    @Autowired
    private ExperimentReplicationVariableValuesDao variableValuesDao;

    @Autowired
    private ExperimentActionExecutionDao actionExecutionDao;

    @Autowired
    private ExperimentEvaluationResultDao evaluationResultDao;

    @Autowired
    private InstantFormatter instantFormatter;

    @Override
    public ResultsType loadResultsFromDatabase(final Experiment experiment) {
        final List<ExperimentReplicationResult> replications = replicationResultDao.findByExperiment(experiment);
        final ResultsType resultsType = OBJECT_FACTORY.createResultsType();
        for (final ExperimentReplicationResult replication : replications) {
            resultsType.getReplication().add(loadResultsFromDatabase(experiment, replication));
        }
        return resultsType;
    }

    private ReplicationType loadResultsFromDatabase(final Experiment experiment,
                                                    final ExperimentReplicationResult replication) {
        final List<ExperimentReplicationVariableValues> variableValues =
                variableValuesDao.findByReplication(replication);
        final ReplicationType replicationType = OBJECT_FACTORY.createReplicationType();
        replicationType.setId(BigInteger.valueOf(replication.getId()));
        for (final ExperimentReplicationVariableValues variableValue : variableValues) {
            replicationType.getIteration().add(loadResultsFromDatabase(experiment, variableValue));
        }
        return replicationType;
    }

    private IterationType loadResultsFromDatabase(final Experiment experiment,
                                                  final ExperimentReplicationVariableValues variableValue) {
        final IterationType iterationType = OBJECT_FACTORY.createIterationType();
        iterationType.setId(BigInteger.valueOf(variableValue.getSequence()));
        final VariableValuesType variableValuesType = asVariableValuesType(variableValue.getVariableValues());
        iterationType.setVariableValues(variableValuesType);

        // We do this to reproduce the original action_block/action structure in the results
        for (final ExperimentActionBlock actionBlock : experiment.getActionBlocks()) {
            final ResultActionBlockType actionBlockType = OBJECT_FACTORY.createResultActionBlockType();
            actionBlockType.setId(actionBlock.getSequence());

            for (final ExperimentAction action : actionBlock.getActions()) {
                final ResultActionType actionType = OBJECT_FACTORY.createResultActionType();
                actionType.setId(action.getSequence());

                final List<ExperimentActionExecution> actionExecutions =
                        actionExecutionDao.getFor(variableValue, action);
                for (final ExperimentActionExecution actionExecution : actionExecutions) {
                    actionType.getInvocation().add(loadResultsFromDatabase(actionExecution));
                }

                if (!actionType.getInvocation().isEmpty()) {
                    actionBlockType.getAction().add(actionType);
                }
            }

            if (!actionBlockType.getAction().isEmpty()) {
                iterationType.getAction_Block().add(actionBlockType);
            }
        }
        return iterationType;
    }

    private InvocationType loadResultsFromDatabase(final ExperimentActionExecution actionExecution) {
        final InvocationType invocationType = OBJECT_FACTORY.createInvocationType();
        invocationType.setId(BigInteger.valueOf(actionExecution.getSequence()));

        final NodeType nodeType = OBJECT_FACTORY.createNodeType();
        nodeType.setId(actionExecution.getNode().getName());
        invocationType.setNode(nodeType);

        invocationType.setAddressValues(asVariableValuesType(actionExecution.getNodeAddressValues()));

        invocationType.setReturnCode(actionExecution.getReturnCode() == null
                ? null
                : BigInteger.valueOf(actionExecution.getReturnCode().longValue()));

        invocationType.setStartedAfter(instantFormatter.print(actionExecution.getStartedAfter(), null));
        invocationType.setEndedBefore(instantFormatter.print(actionExecution.getEndedBefore(), null));

        final List<ExperimentEvaluationResult> evaluationResults =
                evaluationResultDao.findByActionExecution(actionExecution);
        for (final ExperimentEvaluationResult evaluationResult : evaluationResults) {
            try {
                invocationType.getResult().add(objectMapper.writeValueAsString(evaluationResult.getData()));
            } catch (final JsonProcessingException e) {
                LOG.log(Level.WARNING, "Could not write as JSON; result " + evaluationResult + " skipped", e);
            }
        }
        return invocationType;
    }

    private static VariableValuesType asVariableValuesType(final Map<String, String> variableValues) {
        final VariableValuesType variableValuesType = OBJECT_FACTORY.createVariableValuesType();
        if (variableValues != null) {
            for (final Map.Entry<String, String> entry : variableValues.entrySet()) {
                final VariableValueType variableValueType = OBJECT_FACTORY.createVariableValueType();
                variableValueType.setName(entry.getKey());
                variableValueType.setValue(entry.getValue());
                variableValuesType.getVariableValue().add(variableValueType);
            }
        }
        return variableValuesType;
    }
}
