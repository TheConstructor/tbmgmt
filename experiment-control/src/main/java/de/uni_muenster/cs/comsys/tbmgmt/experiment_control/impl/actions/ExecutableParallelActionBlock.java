package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import com.google.common.math.IntMath;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by matthias on 15.11.15.
 */
public class ExecutableParallelActionBlock
        extends AbstractExecutableActionBlock<ActionBlockExecutionState> {

    private int requiredTasks;

    public ExecutableParallelActionBlock(final ExperimentExecutor experimentExecutor,
                                         final ExperimentActionBlock actionBlock) {
        super(experimentExecutor, actionBlock);
        requiredTasks = 0;
    }

    @Override
    public int requiredParallelTasks() {
        if (requiredTasks == 0) {
            requiredTasks = actionBlock.getActions().stream().mapToInt(AbstractExecutableActionBlock::affectedNodeCount)
                                       .reduce(0, IntMath::checkedAdd);
        }
        return requiredTasks;
    }

    @Override
    public ActionBlockExecutionState execute(final ExperimentLogger log, final ScheduledExecutorService executorService,
                                             final Instant startOfIteration,
                                             final ExperimentReplicationVariableValues variableValues,
                                             final String username) {
        final ActionBlockExecutionState state = new ActionBlockExecutionState(requiredParallelTasks());
        for (final ExperimentAction action : actionBlock.getActions()) {
            action.getTargetedNodeGroup().getNodes().stream()
                  .map(node -> executorService.submit(
                          () -> new ExecutableAction(state.getBefore(), node, username, action, variableValues,
                                  experimentExecutor, state.getAfter()).execute()))
                  .forEachOrdered(state.getFutures()::add);
        }
        awaitEndOfExecution(state);
        return state;
    }

}
