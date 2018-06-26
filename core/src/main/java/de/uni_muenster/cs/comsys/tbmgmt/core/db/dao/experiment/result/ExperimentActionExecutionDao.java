package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;

import java.time.Instant;
import java.util.List;

/**
 * Created by matthias on 09.01.16.
 */
public interface ExperimentActionExecutionDao extends GeneratedIdDao<ExperimentActionExecution> {
    int setReturnCode(ExperimentActionExecution experimentActionExecution, Integer returnCode);

    int setEndedBefore(ExperimentActionExecution actionExecution, Instant endTime);

    List<ExperimentActionExecution> getFor(ExperimentReplicationVariableValues variableValues, ExperimentAction action,
                                           Node node);

    List<ExperimentActionExecution> getFor(ExperimentReplicationVariableValues variableValues, ExperimentAction action);
}
