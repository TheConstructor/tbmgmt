package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;

import java.time.Instant;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by matthias on 11.12.15.
 */
public class ExecutableServerActionBlock
        extends AbstractTimedExecutableActionBlock<ExecutableServerActionBlock.ServerExecutionState> {

    public ExecutableServerActionBlock(final ExperimentExecutor experimentExecutor,
                                       final ExperimentActionBlock actionBlock) {
        super(experimentExecutor, actionBlock);
    }

    @Override
    public int requiredParallelTasks() {
        return super.requiredParallelTasks() + 1;
    }

    @Override
    public ServerExecutionState execute(final ExperimentLogger log, final ScheduledExecutorService executorService,
                                        final Instant startOfIteration,
                                        final ExperimentReplicationVariableValues variableValues, String username) {
        final ActionBlockExecutionState executionState =
                innerExecute(log, executorService, startOfIteration, variableValues, username);
        return new ServerExecutionState(executionState,
                executorService.submit(() -> awaitEndOfExecution(executionState)));
    }

    public static class ServerExecutionState extends ActionBlockExecutionState {
        private final Future<?> future;

        public ServerExecutionState(final ActionBlockExecutionState executionState, final Future<?> future) {
            super(executionState.getBefore(), executionState.getAfter(), executionState.getFutures());
            this.future = future;
        }

        public Future<?> getFuture() {
            return future;
        }
    }
}
