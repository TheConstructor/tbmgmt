package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExecutableActionBlock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 15.11.15.
 */
public abstract class AbstractExecutableActionBlock<State> implements ExecutableActionBlock<State> {

    private static final Logger LOG = Logger.getLogger(AbstractExecutableActionBlock.class.getName());

    protected final ExperimentActionBlock actionBlock;
    protected ExperimentExecutor experimentExecutor;

    public AbstractExecutableActionBlock(final ExperimentExecutor experimentExecutor,
                                         final ExperimentActionBlock actionBlock) {
        this.actionBlock = actionBlock;
        this.experimentExecutor = experimentExecutor;
    }

    protected static int affectedNodeCount(final ExperimentAction experimentAction) {
        return experimentAction.getTargetedNodeGroup().getNodes().size();
    }

    public static void awaitEndOfExecution(final ActionBlockExecutionState executionState) {
        try {
            executionState.getAfter().await();
        } catch (final InterruptedException e) {
            try {
                cancelAndWait(executionState);
            } catch (final InterruptedException e1) {
                e.addSuppressed(e1);
            }
            throw new IllegalStateException("Command execution failed.", e);
        }
        for (final Future<?> future : executionState.getFutures()) {
            try {
                future.get();
            } catch (final ExecutionException e) {
                LOG.log(Level.WARNING, "Caught exception from action execution", e);
            } catch (final InterruptedException e) {
                // get() should not need to wait, but it may check Thread.interrupted() anyway
                throw new IllegalStateException(
                        "Caught InterruptedException while waiting on outcome of action execution", e);
            }
        }
    }

    public static void cancelAndWait(final ActionBlockExecutionState executionState) throws InterruptedException {
        // action not already running should not start
        final int neverStartedTasks = executionState.getBefore().drainPermits();
        // actions already running may check for this signal
        executionState.getTerminated().set(true);
        // adjust the countDownLatch by the actions not started because we drained permits
        final CountDownLatch countDownLatch = executionState.getAfter();
        for (int i = 0; i < neverStartedTasks; i++) {
            countDownLatch.countDown();
        }

        try {
            try {
                // Let `terminated` do it's job
                Thread.sleep(5000);
            } finally {
                // Interrupt still running actions - for others this is a NO-OP.
                for (final Future<?> future : executionState.getFutures()) {
                    future.cancel(true);
                }
            }

            countDownLatch.await();
        } catch (final InterruptedException e) {
            // Second time because we can not let an experiment start before all commands of the previous finished
            try {
                countDownLatch.await();
            } catch (final InterruptedException ie) {
                e.addSuppressed(ie);
            }
            throw e;
        }
    }
}
