package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import com.google.common.math.IntMath;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.DurationFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by matthias on 11.12.15.
 */
public abstract class AbstractTimedExecutableActionBlock<State> extends AbstractExecutableActionBlock<State> {
    protected int requiredTasks;

    public AbstractTimedExecutableActionBlock(final ExperimentExecutor experimentExecutor,
                                              final ExperimentActionBlock actionBlock) {
        super(experimentExecutor, actionBlock);
        requiredTasks = 0;
    }

    @Override
    public int requiredParallelTasks() {
        return innerRequiredParallelTasks();
    }

    protected int innerRequiredParallelTasks() {
        if (requiredTasks == 0) {
            requiredTasks = actionBlock
                    .getActions()
                    .stream()
                    .mapToInt(AbstractExecutableActionBlock::affectedNodeCount)
                    .reduce(0, IntMath::checkedAdd);
        }
        return requiredTasks;
    }

    protected ActionBlockExecutionState innerExecute(final ExperimentLogger log,
                                                     final ScheduledExecutorService executorService,
                                                     final Instant startOfIteration,
                                                     final ExperimentReplicationVariableValues variableValues,
                                                     final String username) {
        final ActionBlockExecutionState state = new ActionBlockExecutionState(innerRequiredParallelTasks());
        for (final ExperimentAction action : actionBlock.getActions()) {
            final Instant startOfAction = startOfIteration.plus(action.getStartOffset());

            action
                    .getTargetedNodeGroup()
                    .getNodes()
                    .stream()
                    .map(node -> executorService.schedule(
                            () -> new ExecutableAction(state.getBefore(), node, username, action, variableValues,
                                    experimentExecutor, state.getAfter()).execute(state.getTerminated()),
                            Instant.now().until(startOfAction, ChronoUnit.MILLIS), TimeUnit.MILLISECONDS))
                    .forEachOrdered(state.getFutures()::add);

            final Instant schedulingFinished = Instant.now();
            if (schedulingFinished.isAfter(startOfAction)) {
                log.withAction(action).warning("Start of action " + action
                        + " is probably delayed on some nodes as scheduling finished at startOfIteration + "
                        + DurationFormatter.print(Duration.between(startOfIteration, schedulingFinished)));
            }
        }
        return state;
    }
}
