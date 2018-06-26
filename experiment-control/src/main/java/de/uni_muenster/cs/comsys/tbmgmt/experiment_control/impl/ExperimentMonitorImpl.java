package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.IdleExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogEntryCreator;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.ExperimentMonitor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 13.11.15.
 */
public class ExperimentMonitorImpl implements ExperimentMonitor {

    private static final Logger LOG = Logger.getLogger(ExperimentMonitorImpl.class.getName());
    private final ConcurrentMap<Long, Pair<ExperimentExecutor, Future<ExperimentState>>> scheduledExperiments = new
            ConcurrentHashMap<>();

    /**
     * Local pool to ensure {@link ExperimentMonitorImpl} itself is called even when too many experiments are enqueued
     * for running
     */
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private ExperimentControlConfiguration experimentControlConfiguration;

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private IdleExperimentDao idleExperimentDao;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @Autowired
    private ActionResultStorageHelper actionResultStorageHelper;

    @Autowired
    private InstantFormatter instantFormatter;

    private final AtomicReference<Pair<ExperimentExecutor, Future<ExperimentState>>> idleExperimentRunner =
            new AtomicReference<>();

    @PostConstruct
    public void init() {
        scheduledExecutorService =
                new ScheduledThreadPoolExecutor(experimentControlConfiguration.getMaxParallelExperimentExecutions());
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutorService.shutdownNow();
    }


    @Override
    public synchronized void readExperimentsFromDB() {

        readOnlyTransactionTemplate.execute(transactionState -> {
            final Instant now = Instant.now();
            final List<Experiment> experimentsBehindSchedule = experimentDao.getExperimentsScheduledToCeaseBefore(now);

            // We use the id of the currently running idle-experiment, so even if the selected idle-experiment
            // changes, we let it run until it is: a) finished b) canceled by user or c) terminated when an other
            // experiment is scheduled
            final Long idleExperimentId = Optional
                    .ofNullable(idleExperimentRunner.get())
                    .map(Pair::getLeft)
                    .map(ExperimentExecutor::getExperimentId)
                    .orElse(null);

            for (final Experiment experiment : experimentsBehindSchedule) {
                final Long experimentId = experiment.getId();
                if (Objects.equals(idleExperimentId, experimentId)) {
                    continue;
                }
                LOG.log(Level.FINE, "Retrieved experiment " + experimentId + " which is scheduled to cease at "
                        + experiment.getEndTime());
                if (experimentDao.immediatelySetStateIfNotEnded(experiment, ExperimentState.CANCELATION_REQUESTED)) {
                    actionResultStorageHelper
                            .getLogEntryCreator(experiment, null, null, null, null, null)
                            .createLogEntry(LogEntryCreator.LogReason.WARNING,
                                    "Requested experiment cancellation as it should have ended by "
                                            + instantFormatter.print(experiment.getEndTime(), null));
                }
            }

            final Instant in5Minutes = now.plus(5, ChronoUnit.MINUTES);
            final List<Experiment> experimentsToStartOrCancel =
                    experimentDao.getExperimentsScheduledToRunBefore(in5Minutes);

            for (final Experiment experiment : experimentsToStartOrCancel) {
                final Long experimentId = experiment.getId();
                LOG.log(Level.FINE, () -> "Retrieved experiment " + experimentId + " which is scheduled to run at "
                        + experiment.getStartTime());
                final Pair<ExperimentExecutor, Future<ExperimentState>> previouslyScheduled =
                        scheduledExperiments.get(experimentId);
                if (previouslyScheduled != null) {
                    final ExperimentExecutor experimentExecutor = previouslyScheduled.getLeft();
                    final Future<ExperimentState> future = previouslyScheduled.getRight();
                    if (ExperimentState.CANCELATION_REQUESTED.equals(experiment.getState())) {
                        // Cancelling an already canceled future is a no-op
                        future.cancel(true);
                        continue;
                    } else if (!Objects.equals(experiment.getStartTime(), experimentExecutor.getStartTime())) {
                        future.cancel(true);
                        if (experimentExecutor.isExperimentStarted()) {
                            // So we sadly crossed the point of safe rescheduling...
                            experimentDao.immediatelySetStateIfNotEnded(experiment,
                                    ExperimentState.CANCELATION_REQUESTED);
                            continue;
                        }
                    } else {
                        // Let the experiment proceed. This will most often indicate that we are waiting on a previous
                        // experiment.
                        continue;
                    }
                }

                // Cancelled before being executed. Easy case.
                if (ExperimentState.CANCELATION_REQUESTED.equals(experiment.getState())) {
                    experimentDao.immediatelySetStateIfNotEnded(experiment, ExperimentState.FAILED);
                    continue;
                }

                final ExperimentExecutor experimentExecutor = new ExperimentExecutor(experiment);
                final ScheduledFuture<ExperimentState> future = scheduledExecutorService.schedule(() -> {
                    try {
                        return experimentExecutor.call();
                    } finally {
                        // Clear reference to this execution. == is used to ensure that we don't accidentally
                        // remove a rescheduled experiment run
                        final Pair<ExperimentExecutor, Future<ExperimentState>> current =
                                scheduledExperiments.get(experimentExecutor.getExperimentId());
                        //noinspection ObjectEquality
                        if (current.getLeft() == experimentExecutor) {
                            scheduledExperiments.remove(experimentExecutor.getExperimentId(), current);
                        }
                    }
                }, Instant.now().until(experiment.getStartTime(), ChronoUnit.MILLIS), TimeUnit.MILLISECONDS);
                experimentExecutor.setFuture(future);
                scheduledExperiments.put(experimentId, Pair.of(experimentExecutor, future));
            }

            // Start idleExperiment, if nothing else is running
            if (scheduledExperiments.isEmpty()) {
                if (idleExperimentRunner.get() == null) {
                    final Experiment idleExperiment = idleExperimentDao.getIdleExperiment();
                    if (idleExperiment != null) {
                        // Setting this state so ExperimentExecutor will not choke on an end-state
                        experimentDao.immediatelySetState(idleExperiment, ExperimentState.WAITING_FOR_NODES);
                        final ExperimentExecutor experimentExecutor = new ExperimentExecutor(idleExperiment);
                        final ScheduledFuture<ExperimentState> future = scheduledExecutorService.schedule(() -> {
                                    try {
                                        return experimentExecutor.call();
                                    } finally {
                                        // Clear reference to this execution. == is used to ensure that we don't
                                        // accidentally
                                        // remove a rescheduled experiment run
                                        final Pair<ExperimentExecutor, Future<ExperimentState>> current =
                                                idleExperimentRunner.get();
                                        //noinspection ObjectEquality
                                        if (current.getLeft() == experimentExecutor) {
                                            idleExperimentRunner.compareAndSet(current, null);
                                            scheduledExperiments.remove(experimentExecutor.getExperimentId(), current);
                                        }
                                    }
                                },
                                // We need a delay so we can set up the idleExperimentRunner-value
                                10, TimeUnit.SECONDS);
                        experimentExecutor.setFuture(future);
                        idleExperimentRunner.set(Pair.of(experimentExecutor, future));
                    }
                }
            } else {
                final Pair<ExperimentExecutor, Future<ExperimentState>> current = idleExperimentRunner.get();
                if (current != null) {
                    final ExperimentExecutor experimentExecutor = current.getLeft();
                    final Future<ExperimentState> future = current.getRight();
                    future.cancel(true);
                    if (experimentExecutor.isExperimentStarted()) {
                        final Experiment experiment = experimentDao.find(experimentExecutor.getExperimentId());
                        if (experiment != null) {
                            experimentDao.immediatelySetStateIfNotEnded(experiment,
                                    ExperimentState.CANCELATION_REQUESTED);
                        }
                    }
                    idleExperimentRunner.compareAndSet(current, null);
                }
            }
            return null;
        });
    }
}
