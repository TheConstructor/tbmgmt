package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment.action;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExecutionMode;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.PrefixedValidationContext;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Created by matthias on 17.05.15.
 */
public class FilteredExperimentActionBlock implements Serializable, Validateable {

    private final Experiment            experiment;
    private final ExperimentActionBlock experimentActionBlock;

    public FilteredExperimentActionBlock(final Experiment experiment,
            final ExperimentActionBlock experimentActionBlock) {
        this.experiment = experiment;
        this.experimentActionBlock = experimentActionBlock;
    }

    public BigInteger getSequence() {
        return experimentActionBlock.getSequence();
    }

    public void setSequence(final BigInteger sequence) {
        experimentActionBlock.setSequence(sequence);
    }

    public ExecutionMode getExecutionMode() {
        return experimentActionBlock.getExecutionMode();
    }

    public void setExecutionMode(final ExecutionMode executionMode) {
        experimentActionBlock.setExecutionMode(executionMode);
    }

    @NotNull
    @NotEmpty
    @Valid
    public List<FilteredExperimentAction> getActions() {
        if (experimentActionBlock.getActions() == null) {
            experimentActionBlock.setActions(new ArrayList<>());
        }
        return Collections.unmodifiableList(experimentActionBlock.getActions().stream()
                .map((experimentAction -> new FilteredExperimentAction(experiment, experimentActionBlock,
                        experimentAction
                )))
                .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return experimentActionBlock.toString();
    }

    @Override
    public void validate(final ValidationContext context) {
        PrefixedValidationContext.validateListWithPrefix("actions", getActions(), context);
        if (Objects.equals(getExecutionMode(), ExecutionMode.SERVER)) {
            final long count = experiment
                    .getActionBlocks()
                    .stream()
                    .filter(actionBlock -> actionBlock != experimentActionBlock)
                    .filter(actionBlock -> !Objects.equals(actionBlock.getExecutionMode(), ExecutionMode.SERVER))
                    .limit(2)
                    .count();
            if (count < 2) {
                context
                        .getMessageContext()
                        .addMessage(new MessageBuilder()
                                .error()
                                .source("executionMode")
                                .code("tbmgtm.validation.constraints.twoNonServerExecutingBlocks")
                                .build());
            }
        }
    }
}
