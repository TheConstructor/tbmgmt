package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;

import java.util.List;

/**
 * Created by matthias on 09.01.16.
 */
public interface ExperimentReplicationResultDao extends GeneratedIdDao<ExperimentReplicationResult> {
    List<ExperimentReplicationResult> findByExperiment(Experiment experiment);
}
