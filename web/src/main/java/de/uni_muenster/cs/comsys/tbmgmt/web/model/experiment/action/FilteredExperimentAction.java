package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment.action;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptUtil;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.VariablesUtil;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator.PermutationIterable;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.NameAndDescription;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 Created by matthias on 17.05.15.
 */
@Configurable
public class FilteredExperimentAction implements Serializable, Validateable {

    private final Experiment            experiment;
    private final ExperimentActionBlock experimentActionBlock;
    private final ExperimentAction      experimentAction;

    @Autowired
    private EvaluationScriptResolver evaluationScriptResolver;

    public FilteredExperimentAction(final Experiment experiment, final ExperimentActionBlock experimentActionBlock,
            final ExperimentAction experimentAction) {
        this.experiment = experiment;
        this.experimentActionBlock = experimentActionBlock;
        this.experimentAction = experimentAction;
    }

    public BigInteger getSequence() {
        return experimentAction.getSequence();
    }

    public void setSequence(final BigInteger sequence) {
        experimentAction.setSequence(sequence);
    }

    public String getCommand() {
        return experimentAction.getCommand();
    }

    public void setCommand(final String command) {
        experimentAction.setCommand(command);
    }

    public String getCommandPreview() {
        final String command = experimentAction.getCommand();
        if (!StringUtils.contains(command, '{') || !StringUtils.contains(command, '}')) {
            return null;
        }
        final LinkedHashMap<String, ExperimentVariable> nameToVariable = experiment
                .getVariables()
                .stream()
                .collect(Collectors.toMap(ExperimentVariable::getName, UnaryOperator.identity(), (u, v) -> u,
                        LinkedHashMap::new));
        final PermutationIterable<String, String> variables = new PermutationIterable<>(nameToVariable);
        final Map<Map<String, String>, String> commands =
                experimentAction.getCommands(variables.iterator().next(), experiment::getNodeGroupByName);
        // entrySet() to increase chance of using same values as in getEvaluationParameterPreview
        return commands.isEmpty() ? null : commands.entrySet().iterator().next().getValue();
    }

    public String getTargetedNodeGroupName() {
        final ExperimentNodeGroup targetedNodeGroup = experimentAction.getTargetedNodeGroup();
        return targetedNodeGroup == null ? null : targetedNodeGroup.getName();
    }

    @NotNull
    public Integer getTargetedNodeGroup() {
        final ExperimentNodeGroup targetedNodeGroup = experimentAction.getTargetedNodeGroup();
        final List<ExperimentNodeGroup> nodeGroups = experiment.getNodeGroups();
        for (int i = 0; i < nodeGroups.size(); i++) {
            if (Objects.equals(targetedNodeGroup, nodeGroups.get(i))) {
                return i;
            }
        }
        return null;
    }

    public void setTargetedNodeGroup(final Integer targetedNodeGroup) {
        if (targetedNodeGroup == null || targetedNodeGroup < 0 || targetedNodeGroup >= experiment.getNodeGroups()
                .size()) {
            experimentAction.setTargetedNodeGroup(null);
        } else {
            experimentAction.setTargetedNodeGroup(experiment.getNodeGroups().get(targetedNodeGroup));
        }
    }

    public Duration getStartOffset() {
        return experimentAction.getStartOffset();
    }

    public void setStartOffset(final Duration startOffset) {
        experimentAction.setStartOffset(startOffset);
    }

    public Duration getDuration() {
        return experimentAction.getDuration();
    }

    public void setDuration(final Duration duration) {
        experimentAction.setDuration(duration);
    }

    public Map<String, NameAndDescription> getEvaluationScripts() {
        final List<EvaluationScript> evaluationScripts = evaluationScriptResolver.getAllEvaluationScripts();
        final List<ExperimentFile> files = experiment.getFiles();
        final Map<String, NameAndDescription> map = new TreeMap<>((a, b) -> {
            if (a == null) {
                if (b == null) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (b == null) {
                return 1;
            }
            final int compareToIgnoreCase = a.compareToIgnoreCase(b);
            if (compareToIgnoreCase != 0) {
                return compareToIgnoreCase;
            }
            return a.compareTo(b);
        });
        map.put(null, new NameAndDescription("(none)", "No evaluation; trash STDOUT"));
        for (final EvaluationScript evaluationScript : evaluationScripts) {
            map.put(DesCriptUtil.EVALUATION_SCRIPT_PREFIX + evaluationScript.getFileName(),
                    new NameAndDescription("Evaluation Script: " + evaluationScript.getFileName(),
                            evaluationScript.getDescription()));
        }
        for (int i = 0; i < files.size(); i++) {
            final ExperimentFile file = files.get(i);
            if (file.isEval()) {
                map.put(DesCriptUtil.EVALUATION_FILE_PREFIX + i,
                        new NameAndDescription("Experiment File: " + file.getFileName(), file.getDescription()));
            }
        }
        return map;
    }

    public String getEvaluationScript() {
        final EvaluationScript evaluationScript = experimentAction.getEvaluationScript();
        if (evaluationScript != null) {
            return DesCriptUtil.EVALUATION_SCRIPT_PREFIX + evaluationScript.getFileName();
        }
        final ExperimentFile evaluationFile = experimentAction.getEvaluationFile();
        if (evaluationFile != null) {
            final List<ExperimentFile> files = experiment.getFiles();
            for (int i = 0; i < files.size(); i++) {
                if (Objects.equals(evaluationFile, files.get(i))) {
                    return DesCriptUtil.EVALUATION_FILE_PREFIX + i;
                }
            }
        }
        return null;
    }

    public void setEvaluationScript(final String evaluationScript) {
        if (StringUtils.isBlank(evaluationScript)) {
            experimentAction.setEvaluationScript(null);
            experimentAction.setEvaluationFile(null);
            return;
        }
        if (StringUtils.startsWith(evaluationScript, DesCriptUtil.EVALUATION_SCRIPT_PREFIX)) {
            experimentAction.setEvaluationScript(evaluationScriptResolver.getEvaluationScriptByFileName(
                    evaluationScript.substring(DesCriptUtil.EVALUATION_SCRIPT_PREFIX.length())));
        } else if (StringUtils.startsWith(evaluationScript, DesCriptUtil.EVALUATION_FILE_PREFIX)) {
            final String evaluationFileName = evaluationScript.substring(DesCriptUtil.EVALUATION_FILE_PREFIX.length());
            try {
                experimentAction.setEvaluationFile(experiment.getFiles().get(Integer.valueOf(evaluationFileName)));
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Can not find experiment file by index", e);
            }
        } else {
            throw new IllegalArgumentException("EvaluationScript is not prefixed");
        }
    }

    public Long getEvaluationScriptId() {
        final EvaluationScript evaluationScript = experimentAction.getEvaluationScript();
        return evaluationScript == null ? null : evaluationScript.getId();
    }

    public String getEvaluationParameter() {
        return experimentAction.getEvaluationParameter();
    }

    public void setEvaluationParameter(final String evaluationParameter) {
        experimentAction.setEvaluationParameter(evaluationParameter);
    }

    public String getEvaluationParameterPreview() {
        final String evaluationParameter = experimentAction.getEvaluationParameter();
        if (!StringUtils.contains(evaluationParameter, '{') || !StringUtils.contains(evaluationParameter, '}')) {
            return null;
        }
        final LinkedHashMap<String, ExperimentVariable> nameToVariable = experiment
                .getVariables()
                .stream()
                .collect(Collectors.toMap(ExperimentVariable::getName, UnaryOperator.identity(), (u, v) -> u,
                        LinkedHashMap::new));
        final PermutationIterable<String, String> variables = new PermutationIterable<>(nameToVariable);
        final Map<Map<String, String>, String> commands =
                experimentAction.getCommands(variables.iterator().next(), experiment::getNodeGroupByName);
        if (commands.isEmpty()) {
            return null;
        } else {
            // entrySet() to increase chance of using same values as in getCommandPreview
            final Map.Entry<Map<String, String>, String> entry = commands.entrySet().iterator().next();
            final String baseEvaluationParameter =
                    VariablesUtil.replaceVariables(evaluationParameter, variables.iterator().next());
            final String withAddresses = VariablesUtil.replaceVariables(baseEvaluationParameter, entry.getKey());
            final String withReturnCode =
                    VariablesUtil.replaceVariables(withAddresses, Collections.singletonMap("returnCode", "0"));
            return withReturnCode;
        }
    }

    @Override
    public String toString() {
        return experimentAction.toString();
    }

    @Override
    public void validate(final ValidationContext validationContext) {
        final MessageContext messageContext = validationContext.getMessageContext();
        final ExperimentFile evaluationFile = experimentAction.getEvaluationFile();
        if (evaluationFile != null && !evaluationFile.isEval()) {
            messageContext.addMessage(new MessageBuilder()
                    .error()
                    .source("evaluationScript")
                    .defaultText("File \"" + evaluationFile.getFileName() + "\" is not marked as an evaluation script.")
                    .build());
        }
        if (evaluationFile == null && experimentAction.getEvaluationScript() == null && StringUtils.isNotBlank(
                experimentAction.getEvaluationParameter())) {
            messageContext.addMessage(new MessageBuilder()
                    .error()
                    .source("evaluationParameter")
                    .defaultText("You need to select an evaluation script to use an evaluation parameter.")
                    .build());
        }
    }
}
