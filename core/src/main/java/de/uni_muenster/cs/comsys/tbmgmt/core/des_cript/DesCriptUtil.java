package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * Created by matthias on 24.01.16.
 */
public final class DesCriptUtil {
    public static final String EVALUATION_SCRIPT_PREFIX = "global:";
    public static final String EVALUATION_FILE_PREFIX = "local:";

    public static String extractEvaluationScriptParameter(ExperimentAction action) {
        EvaluationScript evaluationScript = action.getEvaluationScript();
        if (evaluationScript != null) {
            return EVALUATION_SCRIPT_PREFIX + evaluationScript.getFileName();
        }
        ExperimentFile evaluationFile = action.getEvaluationFile();
        if (evaluationFile != null) {
            return EVALUATION_FILE_PREFIX + evaluationFile.getFileName();
        }
        return null;
    }

    public static void extractEvaluationScript(EvaluationScriptResolver evaluationScriptResolver,
                                               Collection<ExperimentFile> evaluationFiles, ExperimentAction action,
                                               String evaluationScriptParameter) {
        if (evaluationScriptParameter == null) {
            action.setEvaluationScript(null);
            action.setEvaluationFile(null);
            return;
        }
        if (StringUtils.startsWith(evaluationScriptParameter, EVALUATION_SCRIPT_PREFIX)) {
            action.setEvaluationScript(evaluationScriptResolver.getEvaluationScriptByFileName(
                    evaluationScriptParameter.substring(EVALUATION_SCRIPT_PREFIX.length())));
        } else if (StringUtils.startsWith(evaluationScriptParameter, EVALUATION_FILE_PREFIX)) {
            String evaluationFileName = evaluationScriptParameter.substring(EVALUATION_FILE_PREFIX.length());
            action.setEvaluationFile(evaluationFiles
                    .stream()
                    .filter(ExperimentFile::isEval)
                    .filter(f -> StringUtils.equals(f.getFileName(), evaluationFileName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Could not find evaluation-file by name of \"" +
                            evaluationFileName + "\"")));
        } else {
            try {
                long id = Long.parseLong(evaluationScriptParameter);
                action.setEvaluationScript(evaluationScriptResolver.getEvaluationScriptById(id));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("EvaluationScript is not prefixed and not parseable as id", e);
            }
        }
    }
}
