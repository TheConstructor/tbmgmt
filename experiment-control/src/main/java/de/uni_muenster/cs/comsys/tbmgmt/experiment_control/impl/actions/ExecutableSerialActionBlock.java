package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
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
public class ExecutableSerialActionBlock extends AbstractExecutableActionBlock<Void> {

    private static final Logger LOG = Logger.getLogger(ExecutableSerialActionBlock.class.getName());

    private int requiredTasks;

    public ExecutableSerialActionBlock(final ExperimentExecutor experimentExecutor,
                                       final ExperimentActionBlock actionBlock) {
        super(experimentExecutor, actionBlock);
        requiredTasks = 0;
    }

    @Override
    public int requiredParallelTasks() {
        if (requiredTasks == 0) {
            requiredTasks = actionBlock.getActions().stream().mapToInt(AbstractExecutableActionBlock::affectedNodeCount)
                                       .max().orElse(0);
        }
        return requiredTasks;
    }

    @Override
    public Void execute(final ExperimentLogger log, final ScheduledExecutorService executorService,
                        final Instant startOfIteration, final ExperimentReplicationVariableValues variableValues,
                        final String username) {
        for (final ExperimentAction action : actionBlock.getActions()) {
            final ActionBlockExecutionState state = new ActionBlockExecutionState(affectedNodeCount(action));
            action.getTargetedNodeGroup().getNodes().stream()
                  .map(node -> executorService.submit(
                          () -> new ExecutableAction(state.getBefore(), node, username, action, variableValues,
                                  experimentExecutor, state.getAfter()).execute()))
                  .forEachOrdered(state.getFutures()::add);
            awaitEndOfExecution(state);
        }
        return null;
    }

}
