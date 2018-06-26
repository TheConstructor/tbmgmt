package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.converters.DurationToLongSecondsConverter;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.VariablesUtil;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator.PermutationIterable;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.math.BigInteger;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
public class ExperimentAction extends GeneratedIdEntity {
    private static final Logger LOG = Logger.getLogger(ExperimentAction.class.getName());

    private ExperimentActionBlock experimentActionBlock;
    private BigInteger sequence;
    private String   command;
    private ExperimentNodeGroup targetedNodeGroup;
    private Duration startOffset;
    private Duration duration;
    private EvaluationScript evaluationScript;
    private ExperimentFile evaluationFile;
    private String   evaluationParameter;

    @ManyToOne
    public ExperimentActionBlock getExperimentActionBlock() {
        return experimentActionBlock;
    }

    public void setExperimentActionBlock(final ExperimentActionBlock experimentActionBlock) {
        this.experimentActionBlock = experimentActionBlock;
    }

    @Basic
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Column(length = 10000)
    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    @ManyToOne(optional = false)
    public ExperimentNodeGroup getTargetedNodeGroup() {
        return targetedNodeGroup;
    }

    public void setTargetedNodeGroup(final ExperimentNodeGroup targetedNodeGroup) {
        this.targetedNodeGroup = targetedNodeGroup;
    }

    @Basic
    @Convert(converter = DurationToLongSecondsConverter.class)
    public Duration getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(final Duration startOffset) {
        this.startOffset = startOffset;
    }

    @Basic
    @Convert(converter = DurationToLongSecondsConverter.class)
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(final Duration duration) {
        this.duration = duration;
    }

    @ManyToOne(optional = true)
    public EvaluationScript getEvaluationScript() {
        return evaluationScript;
    }

    public void setEvaluationScript(final EvaluationScript evaluationScript) {
        this.evaluationScript = evaluationScript;
        if (evaluationScript != null) {
            evaluationFile = null;
        }
    }

    @ManyToOne(optional = true)
    public ExperimentFile getEvaluationFile() {
        return evaluationFile;
    }

    public void setEvaluationFile(final ExperimentFile evaluationFile) {
        this.evaluationFile = evaluationFile;
        if (evaluationFile != null) {
            evaluationScript = null;
        }
    }

    @Basic
    @Column(length = 10000)
    public String getEvaluationParameter() {
        return evaluationParameter;
    }

    public void setEvaluationParameter(final String evaluationParameter) {
        this.evaluationParameter = evaluationParameter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("sequence", sequence)
                                        .append("command", command)
                                        .append("targetedNodeGroup", targetedNodeGroup)
                                        .append("startOffset", startOffset)
                                        .append("duration", duration)
                                        .append("evaluationScript", evaluationScript)
                                        .append("evaluationFile", evaluationFile)
                                        .append("evaluationParameter", evaluationParameter)
                                        .toString();
    }

    @Transient
    public ExperimentAction createCopy(final ExperimentActionBlock experimentActionBlock,
                                       final Function<String, ExperimentNodeGroup> nodeGroupResolver) {
        final ExperimentAction experimentAction = new ExperimentAction();
        experimentAction.setExperimentActionBlock(experimentActionBlock);
        experimentAction.setSequence(getSequence());
        experimentAction.setCommand(getCommand());
        experimentAction.setTargetedNodeGroup(nodeGroupResolver.apply(getTargetedNodeGroup().getName()));
        experimentAction.setStartOffset(getStartOffset());
        experimentAction.setDuration(getDuration());
        experimentAction.setEvaluationScript(getEvaluationScript());
        final ExperimentFile evaluationFile = getEvaluationFile();
        if (evaluationFile != null) {
            final String fileName = evaluationFile.getFileName();
            experimentAction.setEvaluationFile(experimentActionBlock
                    .getExperiment()
                    .getFiles()
                    .stream()
                    .filter(f -> fileName.equals(f.getFileName()))
                    .findFirst()
                    .orElse(null));
        }
        experimentAction.setEvaluationParameter(getEvaluationParameter());
        return experimentAction;
    }

    @Transient
    public Map<Map<String, String>, String> getCommands(final Map<String, String> variableMap,
                                                        final Function<String, ExperimentNodeGroup> nodeGroupResolver) {
        final String baseCommand = VariablesUtil.replaceVariables(getCommand(), variableMap);

        final Map<String, List<String>> nodeGroupReferences =
                VariablesUtil.extractNodeGroupReferences(baseCommand, nodeGroupResolver);

        final Map<Map<String, String>, String> commands = new LinkedHashMap<>();
        for (final Map<String, String> mappings : new PermutationIterable<>(nodeGroupReferences)) {
            commands.put(mappings, VariablesUtil.replaceVariables(baseCommand, mappings));
        }
        return commands;
    }
}
