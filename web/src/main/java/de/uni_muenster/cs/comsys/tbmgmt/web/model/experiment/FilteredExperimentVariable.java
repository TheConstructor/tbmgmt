package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableRange;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableValue;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.VariableValueType;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.EnumUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.MessageFormatMessageResolver;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import org.hibernate.validator.constraints.Length;
import org.springframework.binding.message.Severity;
import org.springframework.binding.validation.ValidationContext;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by matthias on 25.02.16.
 */
public class FilteredExperimentVariable implements Serializable, Validateable {

    private final Experiment experiment;
    private final ExperimentVariable experimentVariable;

    public FilteredExperimentVariable(final Experiment experiment, final ExperimentVariable experimentVariable) {
        this.experiment = experiment;
        this.experimentVariable = experimentVariable;
    }

    @NotBlank
    @Pattern(regexp = "[-._a-zA-Z0-9]+")
    @Length(min = 1, max = 42)
    public String getName() {
        return experimentVariable.getName();
    }

    public void setName(final String name) {
        experimentVariable.setName(name);
    }

    @NotNull
    public String getType() {
        return EnumUtil.nullsafeGetName(experimentVariable.getType());
    }

    public void setType(final String type) {
        experimentVariable.setType(VariableValueType.fromName(type));
    }

    @Pattern(regexp = "[+-]?[0-9]+(?:\\.[0-9]+)?|")
    public String getRangeStart() {
        final ExperimentVariableRange range = experimentVariable.getRange();
        if (range == null) {
            return null;
        }
        return range.getStart();
    }

    public void setRangeStart(final String rangeStart) {
        final ExperimentVariableRange range = experimentVariable.getRange();
        if (range == null) {
            if (StringUtils.isNotBlank(rangeStart)) {
                final ExperimentVariableRange newRange = new ExperimentVariableRange();
                newRange.setExperimentVariable(experimentVariable);
                experimentVariable.setRange(newRange);
                newRange.setStart(rangeStart);
            }
        } else {
            if (StringUtils.isNotBlank(rangeStart)) {
                range.setStart(rangeStart);
            } else if (StringUtils.isBlank(range.getEnd())) {
                experimentVariable.setRange(null);
            } else {
                range.setStart(null);
            }
        }
    }

    @Pattern(regexp = "[+-]?[0-9]+(?:\\.[0-9]+)?|")
    public String getRangeEnd() {
        final ExperimentVariableRange range = experimentVariable.getRange();
        if (range == null) {
            return null;
        }
        return range.getEnd();
    }

    public void setRangeEnd(final String rangeEnd) {
        final ExperimentVariableRange range = experimentVariable.getRange();
        if (range == null) {
            if (StringUtils.isNotBlank(rangeEnd)) {
                final ExperimentVariableRange newRange = new ExperimentVariableRange();
                newRange.setExperimentVariable(experimentVariable);
                experimentVariable.setRange(newRange);
                newRange.setEnd(rangeEnd);
            }
        } else {
            if (StringUtils.isNotBlank(rangeEnd)) {
                range.setEnd(rangeEnd);
            } else if (StringUtils.isBlank(range.getStart())) {
                experimentVariable.setRange(null);
            } else {
                range.setEnd(null);
            }
        }
    }

    @Pattern(regexp = "[+-]?[0-9]+(?:\\.[0-9]+)?|")
    public String getStepping() {
        return experimentVariable.getStepping();
    }

    public void setStepping(final String stepping) {
        experimentVariable.setStepping(stepping);
    }

    @Valid
    public List<FilteredExperimentVariableValue> getValues() {
        if (experimentVariable.getValues() == null) {
            experimentVariable.setValues(new ArrayList<>());
        }

        return Collections.unmodifiableList(experimentVariable
                .getValues()
                .stream()
                .map((experimentVariableValue -> new FilteredExperimentVariableValue(experiment,
                        experimentVariableValue)))
                .collect(Collectors.toList()));
    }

    @Override
    public void validate(final ValidationContext context) {
        final VariableValueType type = experimentVariable.getType();
        final ExperimentVariableRange range = experimentVariable.getRange();
        final List<ExperimentVariableValue> values = experimentVariable.getValues();
        if (type != null) {
            switch (type) {
                case SET: {
                    if (range != null) {
                        if (StringUtils.isNotBlank(range.getStart())) {
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeStart",
                                            new String[]{"tbmgtm.validation.constraints.emptyOrNotTypeX"},
                                            Severity.ERROR, new Object[]{type.getDisplayName()}));
                        }
                        if (StringUtils.isNotBlank(range.getEnd())) {
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeEnd",
                                            new String[]{"tbmgtm.validation.constraints.emptyOrNotTypeX"},
                                            Severity.ERROR, new Object[]{type.getDisplayName()}));
                        }
                    }
                    if (StringUtils.isNotBlank(experimentVariable.getStepping())) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("stepping",
                                        new String[]{"tbmgtm.validation.constraints.emptyOrNotTypeX"}, Severity.ERROR,
                                        new Object[]{type.getDisplayName()}));
                    }
                    if (values == null || values.isEmpty()) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("values",
                                        new String[]{"tbmgtm.validation.constraints.notEmptyOrNotTypeX"},
                                        Severity.ERROR, new Object[]{type.getDisplayName()}));
                    }
                    break;
                }
                case INTEGER: {
                    if (range != null) {
                        if (StringUtils.contains(range.getStart(), '.')) {
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeStart",
                                            new String[]{"tbmgtm.validation.constraints.noDotOrNotTypeX"},
                                            Severity.ERROR, new Object[]{type.getDisplayName()}));
                        }
                        if (StringUtils.contains(range.getEnd(), '.')) {
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeEnd",
                                            new String[]{"tbmgtm.validation.constraints.noDotOrNotTypeX"},
                                            Severity.ERROR, new Object[]{type.getDisplayName()}));
                        }
                    }
                    if (StringUtils.contains(experimentVariable.getStepping(), '.')) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("stepping",
                                        new String[]{"tbmgtm.validation.constraints.noDotOrNotTypeX"}, Severity.ERROR,
                                        new Object[]{type.getDisplayName()}));
                    }
                }
                //noinspection fallthrough
                case DOUBLE: {
                    if (range == null || StringUtils.isBlank(range.getStart())) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("rangeStart",
                                        new String[]{"tbmgtm.validation.constraints.notEmptyOrNotTypeX"},
                                        Severity.ERROR, new Object[]{type.getDisplayName()}));
                    }
                    if (range == null || StringUtils.isBlank(range.getEnd())) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("rangeEnd",
                                        new String[]{"tbmgtm.validation.constraints.notEmptyOrNotTypeX"},
                                        Severity.ERROR, new Object[]{type.getDisplayName()}));
                    }
                    if (StringUtils.isBlank(experimentVariable.getStepping())) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("stepping",
                                        new String[]{"tbmgtm.validation.constraints.notEmptyOrNotTypeX"},
                                        Severity.ERROR, new Object[]{type.getDisplayName()}));
                    }
                    if (range != null && StringUtils.isNoneBlank(range.getStart(), range.getEnd(),
                            experimentVariable.getStepping())) {
                        BigDecimal start;
                        try {
                            start = new BigDecimal(range.getStart());
                        } catch (final NumberFormatException e) {
                            start = null;
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeStart",
                                            new String[]{"tbmgtm.validation.constraints.number"}, Severity.ERROR,
                                            new Object[0]));
                        }
                        BigDecimal end;
                        try {
                            end = new BigDecimal(range.getEnd());
                        } catch (final NumberFormatException e) {
                            end = null;
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("rangeEnd",
                                            new String[]{"tbmgtm.validation.constraints.number"}, Severity.ERROR,
                                            new Object[0]));
                        }
                        BigDecimal stepping;
                        try {
                            stepping = new BigDecimal(experimentVariable.getStepping());
                            if (stepping.compareTo(BigDecimal.ZERO) == 0) {
                                context
                                        .getMessageContext()
                                        .addMessage(new MessageFormatMessageResolver("stepping",
                                                new String[]{"tbmgtm.validation.constraints.notZero"}, Severity.ERROR,
                                                new Object[0]));
                                stepping = null;
                            }
                        } catch (final NumberFormatException e) {
                            stepping = null;
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("stepping",
                                            new String[]{"tbmgtm.validation.constraints.number"}, Severity.ERROR,
                                            new Object[0]));
                        }
                        if (start != null && end != null && stepping != null) {
                            final boolean startGreaterThanEnd = start.compareTo(end) > 0;
                            final boolean steppingPositive = stepping.compareTo(BigDecimal.ZERO) > 0;
                            if (startGreaterThanEnd == steppingPositive) {
                                context
                                        .getMessageContext()
                                        .addMessage(new MessageFormatMessageResolver("stepping",
                                                new String[]{"tbmgtm.validation.constraints.wrongSteppingSign"},
                                                Severity.ERROR, new Object[0]));
                            }
                        }
                    }
                    if (values != null && !values.isEmpty()) {
                        context
                                .getMessageContext()
                                .addMessage(new MessageFormatMessageResolver("values",
                                        new String[]{"tbmgtm.validation.constraints.emptyOrNotTypeX"}, Severity.ERROR,
                                        new Object[]{type.getDisplayName()}));
                        for (int i = 0; i < values.size(); i++) {
                            context
                                    .getMessageContext()
                                    .addMessage(new MessageFormatMessageResolver("values[" + i + "].value",
                                            new String[]{"tbmgtm.validation.constraints.deleteOrNotTypeX"},
                                            Severity.ERROR, new Object[]{type.getDisplayName()}));
                        }
                    }
                    break;
                }
            }
        }
    }
}
