package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by matthias on 24.01.16.
 */
public class EvaluationScriptResolverMock implements EvaluationScriptResolver {
    private final ConcurrentMap<Long, EvaluationScript> evaluationScripts = new ConcurrentHashMap<>();

    @Override
    public EvaluationScript getEvaluationScriptByFileName(String fileName) {
        return evaluationScripts
                .values()
                .stream()
                .filter(e -> StringUtils.equals(e.getFileName(), fileName))
                .findFirst()
                .orElseGet(() -> {
                    long id = Objects.hashCode(fileName);
                    EvaluationScript evaluationScript = new EvaluationScript();
                    evaluationScript.setId(id);
                    evaluationScript.setFileName(fileName);
                    evaluationScripts.putIfAbsent(id, evaluationScript);
                    return evaluationScripts.get(id);
                });
    }

    @Override
    public EvaluationScript getEvaluationScriptById(Long id) {
        return evaluationScripts.computeIfAbsent(id, (i) -> {
            EvaluationScript evaluationScript = new EvaluationScript();
            evaluationScript.setId(i);
            evaluationScript.setFileName(String.valueOf(i) + ".file");
            return evaluationScript;
        });
    }

    @Override
    public List<EvaluationScript> getAllEvaluationScripts() {
        return new ArrayList<>(evaluationScripts.values());
    }
}
