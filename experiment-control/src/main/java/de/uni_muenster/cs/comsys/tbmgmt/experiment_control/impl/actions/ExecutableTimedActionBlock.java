package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

/**
 * Created by matthias on 15.11.15.
 */
public class ExecutableTimedActionBlock
        extends AbstractTimedExecutableActionBlock<Void> {

    private static final Logger LOG = Logger.getLogger(ExecutableTimedActionBlock.class.getName());

    public ExecutableTimedActionBlock(final ExperimentExecutor experimentExecutor,
                                      final ExperimentActionBlock actionBlock) {
        super(experimentExecutor, actionBlock);
    }

    @Override
    public Void execute(final ExperimentLogger log, final ScheduledExecutorService executorService,
                        final Instant startOfIteration, final ExperimentReplicationVariableValues variableValues,
                        final String username) {
        final ActionBlockExecutionState executionState =
                 innerExecute(log, executorService, startOfIteration, variableValues, username);
        awaitEndOfExecution(executionState);
        return null;
    }

}
