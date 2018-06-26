package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TagDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.ConvertingList;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment.action.FilteredExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.PrefixedValidationContext;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 Created by matthias on 23.04.15.
 */
@Configurable
public class FilteredExperiment implements Serializable, Validateable {
    private final Experiment experiment;
    private String activeTab = "general";

    @Autowired
    private transient TagDao tagDao;
    @Autowired
    private transient ExperimentDao experimentDao;
    @Autowired
    private transient InstantFormatter instantFormatter;

    public FilteredExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    public Long getId() {
        return experiment.getId();
    }

    @NotNull
    @NotBlank
    public String getName() {
        return experiment.getName();
    }

    public void setName(final String name) {
        experiment.setName(name);
    }

    @NotNull
    @NotBlank
    public String getDescription() {
        return experiment.getDescription();
    }

    public void setDescription(final String description) {
        experiment.setDescription(description);
    }

    @Future
    public Instant getStartTime() {
        return experiment.getStartTime();
    }

    public void setStartTime(final Instant startTime) {
        experiment.setStartTime(startTime);
    }

    public Duration getDuration() {
        return experiment.getDuration();
    }

    public void setDuration(final Duration duration) {
        experiment.setDuration(duration);
    }

    @Min(1)
    public long getReplications() {
        return experiment.getReplications();
    }

    public void setReplications(final long replications) {
        experiment.setReplications(replications);
    }

    public Duration getPauseBetweenReplications() {
        return experiment.getPauseBetweenReplications();
    }

    public void setPauseBetweenReplications(final Duration pauseBetweenReplications) {
        experiment.setPauseBetweenReplications(pauseBetweenReplications);
    }

    public Duration getSampleInterval() {
        return experiment.getSampleInterval();
    }

    public void setSampleInterval(final Duration sampleInterval) {
        experiment.setSampleInterval(sampleInterval);
    }

    public boolean isRestartNodes() {
        return experiment.isRestartNodes();
    }

    public void setRestartNodes(final boolean restartNodes) {
        experiment.setRestartNodes(restartNodes);
    }

    public boolean isLockTestbed() {
        return experiment.isLockTestbed();
    }

    public void setLockTestbed(final boolean lockTestbed) {
        experiment.setLockTestbed(lockTestbed);
    }

    public boolean isInteractive() {
        return experiment.isInteractive();
    }

    public void setInteractive(final boolean interactive) {
        experiment.setInteractive(interactive);
    }

    @NotNull
    @NotEmpty
    @Valid
    public List<FilteredExperimentNodeGroup> getNodeGroups() {
        if (experiment.getNodeGroups() == null) {
            experiment.setNodeGroups(new ArrayList<>());
        }
        return Collections.unmodifiableList(experiment.getNodeGroups().stream()
                .map((experimentNodeGroup -> new FilteredExperimentNodeGroup(experiment, experimentNodeGroup
                )))
                .collect(Collectors.toList()));
    }

    @Valid
    public List<FilteredExperimentFile> getFiles() {
        if (experiment.getFiles() == null) {
            experiment.setFiles(new ArrayList<>());
        }
        return Collections.unmodifiableList(experiment.getFiles().stream()
                .map((experimentFile -> new FilteredExperimentFile(experiment, experimentFile
                )))
                .collect(Collectors.toList()));
    }

    @Valid
    public List<FilteredExperimentVariable> getVariables() {
        if (experiment.getVariables() == null) {
            experiment.setVariables(new ArrayList<>());
        }
        return Collections.unmodifiableList(experiment
                .getVariables()
                .stream()
                .map((experimentVariable -> new FilteredExperimentVariable(experiment, experimentVariable)))
                .collect(Collectors.toList()));
    }

    @Valid
    public List<FilteredExperimentActionBlock> getActionBlocks() {
        if (experiment.getActionBlocks() == null) {
            experiment.setActionBlocks(new ArrayList<>());
        }
        return Collections.unmodifiableList(experiment.getActionBlocks().stream()
                .map((experimentActionBlock -> new FilteredExperimentActionBlock(experiment, experimentActionBlock
                )))
                .collect(Collectors.toList()));
    }

    public List<String> getTags() {
        if (experiment.getTags() == null) {
            experiment.setTags(new ArrayList<>());
        }
        return new ConvertingList<>(experiment.getTags(), Tag::getName, getNameToTagResolver());
    }

    public void setTags(final List<String> tags) {
        if (tags == null) {
            experiment.setTags(new ArrayList<>());
        } else {
            experiment.setTags(tags
                    .stream()
                    .map(getNameToTagResolver())
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    public Function<String, Tag> getNameToTagResolver() {
        return (name) -> {
            try {
                return tagDao.getByName(name);
            } catch (NoSuchElementException | EmptyResultDataAccessException e) {
                final Tag tag = new Tag();
                tag.setName(name);
                return tag;
            }
        };
    }

    /**
     UI-Only

     @return the active tab during Experiment-editing
     */
    public String getActiveTab() {
        return activeTab;
    }

    /**
     UI-Only

     @param activeTab the active tab during Experiment-editing
     */
    public void setActiveTab(final String activeTab) {
        this.activeTab = activeTab;
    }

    public List<String> getPossiblyConflictingExperiments() {
        final Instant startTime = getStartTime();
        final Duration duration = getDuration();
        if (startTime == null || duration == null) {
            return Collections.emptyList();
        }
        final List<Experiment> conflictingExperiments =
                experimentDao.getPossiblyConflictingExperiments(experiment.getId(), startTime, duration,
                        experiment.getUsedNodeIds());
        final ArrayList<String> strings = new ArrayList<>(conflictingExperiments.size());
        for (final Experiment conflictingExperiment : conflictingExperiments) {
            strings.add(String.format("\"%s\" (id: %d, start: %s, end: %s)", conflictingExperiment.getName(),
                    conflictingExperiment.getId(), instantFormatter.print(conflictingExperiment.getStartTime(), null),
                    instantFormatter.print(conflictingExperiment.getEndTime(), null)));
        }
        return strings;
    }

    @Override
    public String toString() {
        return experiment.toString();
    }

    @Override
    public void validate(final ValidationContext context) {
        final Duration duration = getDuration();
        final Instant startTime = getStartTime();
        if (duration != null) {
            if (startTime == null) {
                context
                        .getMessageContext()
                        .addMessage(new MessageBuilder()
                                .error()
                                .source("duration")
                                .code("tbmgtm.validation.constraints.startDateRequired")
                                .build());
            } else if (duration.compareTo(Duration.ZERO) <= 0) {
                context
                        .getMessageContext()
                        .addMessage(new MessageBuilder()
                                .error()
                                .source("duration")
                                .code("tbmgtm.validation.constraints.positive")
                                .build());
            }
        }
        if (isInteractive()) {
            final List<FilteredExperimentActionBlock> actionBlocks = getActionBlocks();
            if (actionBlocks != null && !actionBlocks.isEmpty()) {
                context.getMessageContext().addMessage(new MessageBuilder().error().source("actionBlocks")
                        .code("tbmgtm.validation.constraints.emptyOrNonInteractive").build());
                context.getMessageContext().addMessage(new MessageBuilder().error().source("interactive")
                        .code("tbmgtm.validation.constraints.nonInteractiveOrNoActions").build());
            }
        } else {
            final List<FilteredExperimentActionBlock> actionBlocks = getActionBlocks();
            if (actionBlocks == null || actionBlocks.isEmpty()) {
                context.getMessageContext().addMessage(new MessageBuilder().error().source("actionBlocks")
                        .code("tbmgtm.validation.constraints.notEmptyOrInteractive").build());
                context.getMessageContext().addMessage(new MessageBuilder().error().source("interactive")
                        .code("tbmgtm.validation.constraints.interactiveOrActions").build());
            }
        }
        PrefixedValidationContext.validateListWithPrefix("nodeGroups", getNodeGroups(), context);
        PrefixedValidationContext.validateListWithPrefix("actionBlocks", getActionBlocks(), context);
        PrefixedValidationContext.validateListWithPrefix("variables", getVariables(), context);
        PrefixedValidationContext.validateListWithPrefix("files", getFiles(), context);
    }
}
