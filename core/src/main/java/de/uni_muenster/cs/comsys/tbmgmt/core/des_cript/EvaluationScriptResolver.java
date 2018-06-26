package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;

import java.util.List;

/**
 * Created by matthias on 16.03.15.
 */
public interface EvaluationScriptResolver {
    EvaluationScript getEvaluationScriptByFileName(String fileName);

    EvaluationScript getEvaluationScriptById(Long id);

    List<EvaluationScript> getAllEvaluationScripts();
}
