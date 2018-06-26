package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by matthias on 13.11.15.
 */
public interface ExecutableActionBlock<State> {
    int requiredParallelTasks();

    State execute(ExperimentLogger log, ScheduledExecutorService executorService, Instant startOfIteration,
                  ExperimentReplicationVariableValues variableValues, String username);
}
