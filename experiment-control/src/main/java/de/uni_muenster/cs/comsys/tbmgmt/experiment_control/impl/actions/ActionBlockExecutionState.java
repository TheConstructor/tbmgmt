package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.actions;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by matthias on 29.02.16.
 */
public class ActionBlockExecutionState {
    private final Semaphore before;
    private final CountDownLatch after;
    private final List<Future<List<ExperimentActionExecution>>> futures;
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    public ActionBlockExecutionState(final int requiredParallelTasks) {
        this(new Semaphore(requiredParallelTasks), new CountDownLatch(requiredParallelTasks),
                new ArrayList<>(requiredParallelTasks));
    }

    public ActionBlockExecutionState(final Semaphore before, final CountDownLatch after,
                                     final List<Future<List<ExperimentActionExecution>>> futures) {
        this.before = before;
        this.after = after;
        this.futures = futures;
    }

    public Semaphore getBefore() {
        return before;
    }

    public CountDownLatch getAfter() {
        return after;
    }

    public List<Future<List<ExperimentActionExecution>>> getFutures() {
        return futures;
    }

    public AtomicBoolean getTerminated() {
        return terminated;
    }
}
