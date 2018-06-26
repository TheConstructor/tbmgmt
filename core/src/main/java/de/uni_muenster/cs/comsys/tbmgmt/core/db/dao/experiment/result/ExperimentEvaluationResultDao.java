package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult;

import java.util.List;

/**
 * Created by matthias on 09.01.16.
 */
public interface ExperimentEvaluationResultDao extends GeneratedIdDao<ExperimentEvaluationResult> {
    List<ExperimentEvaluationResult> findByActionExecution(ExperimentActionExecution actionExecution);
}
