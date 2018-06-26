package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolver;

/**
 * Created by matthias on 09.01.16.
 */
public interface EvaluationScriptDao extends GeneratedIdDao<EvaluationScript>, EvaluationScriptResolver {
    Long countUsages(EvaluationScript evaluationScript);
}
