package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;

import java.util.List;

/**
 * Created by matthias on 09.01.16.
 */
public interface ExperimentReplicationVariableValuesDao extends GeneratedIdDao<ExperimentReplicationVariableValues> {
    List<ExperimentReplicationVariableValues> findByReplication(ExperimentReplicationResult replication);
}
