package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 Created by matthias on 16.03.15.
 */
public interface ExperimentDao extends GeneratedIdDao<Experiment> {
    List<Experiment> getExperimentsScheduledToRunBefore(Instant before);

    List<Experiment> getExperimentsScheduledToCeaseBefore(Instant before);

    ExperimentState immediatelySetState(Experiment experiment, ExperimentState state);

    /**
     * Set the state of experiment if it is not already in an final state.
     *
     * @return {@code true}, if the state was changed
     */
    boolean immediatelySetStateIfNotEnded(Experiment experiment, ExperimentState state);

    List<Experiment> getConflictingExperiments(Experiment experiment);

    List<Experiment> getPossiblyConflictingExperiments(Long id, Instant startTime, Duration duration,
                                                       Set<Long> nodeIds);
}
