package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.converters.DurationToLongSecondsConverter;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariable;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by matthias on 10.03.15.
 */
@Entity
@Cacheable(false)
public class Experiment extends GeneratedIdEntity {
    private String name;
    private String description;
    private Instant startTime;
    private Instant endTime;
    @Transient
    private Duration duration;
    private long replications;
    private Duration pauseBetweenReplications;
    private Duration sampleInterval;
    private boolean restartNodes;
    private boolean lockTestbed;
    private boolean interactive;
    private ExperimentState state = ExperimentState.SCHEDULED;
    /**
     * User-ID of creator. Not the {@link User}-object to prevent cascading deletes, ...
     */
    private Long creatorId;
    private List<ExperimentNodeGroup>   nodeGroups   = new ArrayList<>();
    private List<ExperimentFile>        files        = new ArrayList<>();
    private List<ExperimentVariable>    variables    = new ArrayList<>();
    private List<ExperimentActionBlock> actionBlocks = new ArrayList<>();
    private List<ExperimentReplicationResult> replicationResults = new ArrayList<>();
    private List<Tag> tags = new ArrayList<>();

    @Basic
    @Column(length = 1000)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Basic
    @Column(columnDefinition = "text")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Basic
    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(final Instant startTime) {
        this.startTime = startTime;
        if (startTime != null) {
            if (duration != null) {
                endTime = startTime.plus(duration);
            } else if (endTime != null) {
                calculateDuration();
            }
        } else {
            duration = null;
        }
    }

    @Basic
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(final Instant endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            if (startTime != null) {
                calculateDuration();
            } else if (duration != null) {
                startTime = endTime.minus(duration);
            }
        } else {
            duration = null;
        }
    }

    @Transient
    private void calculateDuration() {
        if (startTime == null || endTime == null) {
            return;
        }
        duration = Duration.between(startTime, endTime);
    }

    @Transient
    public Duration getDuration() {
        return duration;
    }

    @Transient
    public void setDuration(final Duration duration) {
        this.duration = duration;
        if (duration == null) {
            if (startTime != null && endTime != null) {
                endTime = null;
            }
        } else {
            if (startTime != null) {
                endTime = startTime.plus(duration);
            } else if (endTime != null) {
                startTime = endTime.minus(duration);
            }
        }
    }

    @Basic
    public long getReplications() {
        return replications;
    }

    public void setReplications(final long replications) {
        this.replications = replications;
    }

    @Basic
    @Convert(converter = DurationToLongSecondsConverter.class)
    public Duration getPauseBetweenReplications() {
        return pauseBetweenReplications;
    }

    public void setPauseBetweenReplications(final Duration pauseBetweenReplications) {
        this.pauseBetweenReplications = pauseBetweenReplications;
    }

    @Basic
    @Convert(converter = DurationToLongSecondsConverter.class)
    public Duration getSampleInterval() {
        return sampleInterval;
    }

    public void setSampleInterval(final Duration sampleInterval) {
        this.sampleInterval = sampleInterval;
    }

    @Basic
    public boolean isRestartNodes() {
        return restartNodes;
    }

    public void setRestartNodes(final boolean restartNodes) {
        this.restartNodes = restartNodes;
    }

    @Basic
    public boolean isLockTestbed() {
        return lockTestbed;
    }

    public void setLockTestbed(final boolean lockTestbed) {
        this.lockTestbed = lockTestbed;
    }

    @Basic
    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(final boolean interactive) {
        this.interactive = interactive;
    }

    @Basic
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExperimentState getState() {
        return state;
    }

    public void setState(final ExperimentState state) {
        this.state = state;
    }

    @Basic
    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(final Long creator) {
        this.creatorId = creator;
    }

    @Transient
    public ExperimentNodeGroup getNodeGroupByName(final String name) {
        for (final ExperimentNodeGroup experimentNodeGroup : getNodeGroups()) {
            if (Objects.equals(experimentNodeGroup.getName(), name)) {
                return experimentNodeGroup;
            }
        }
        return null;
    }

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ExperimentNodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(final List<ExperimentNodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ExperimentFile> getFiles() {
        return files;
    }

    public void setFiles(final List<ExperimentFile> files) {
        this.files = files;
    }

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ExperimentVariable> getVariables() {
        return variables;
    }

    public void setVariables(final List<ExperimentVariable> variables) {
        this.variables = variables;
    }

    @ManyToMany()
    @OrderBy("name")
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(final List<Tag> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("name", name)
                                        .append("description", description)
                                        .append("startTime", startTime).append("endTime", endTime)
                                        .append("replications", replications)
                                        .append("pauseBetweenReplications", pauseBetweenReplications)
                                        .append("sampleInterval", sampleInterval)
                                        .append("restartNodes", restartNodes)
                                        .append("lockTestbed", lockTestbed)
                                        .append("interactive", interactive)
                                        .append("state", state)
                                        .append("creatorId", creatorId)
                                        .append("nodeGroups", nodeGroups)
                                        .append("files", files)
                                        .append("variables", variables)
                                        .append("actionBlocks", actionBlocks)
                                        .append("replicationResults", replicationResults)
                                        .toString();
    }

    @Transient
    public boolean isNodeGroupInUse(final ExperimentNodeGroup experimentNodeGroup) {
        for (final ExperimentActionBlock actionBlock : getActionBlocks()) {
            if (actionBlock.isNodeGroupInUse(experimentNodeGroup)) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "sequence asc")
    public List<ExperimentActionBlock> getActionBlocks() {
        return actionBlocks;
    }

    public void setActionBlocks(final List<ExperimentActionBlock> actionBlocks) {
        this.actionBlocks = actionBlocks;
    }

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "sequence asc")
    public List<ExperimentReplicationResult> getReplicationResults() {
        return replicationResults;
    }

    public void setReplicationResults(final List<ExperimentReplicationResult> replicationResults) {
        this.replicationResults = replicationResults;
    }

    @Transient
    public void generateActionBlockSequence() {
        BigInteger current = BigInteger.ZERO;
        for (final ExperimentActionBlock actionBlock : getActionBlocks()) {
            actionBlock.setSequence(current);
            current = current.add(BigInteger.ONE);
        }
    }

    @Transient
    public Set<Long> getUsedNodeIds() {
        final Set<Long> ids = new HashSet<>();
        for (final ExperimentNodeGroup experimentNodeGroup : getNodeGroups()) {
            for (final Node node : experimentNodeGroup.getNodes()) {
                ids.add(node.getId());
            }
        }
        return ids;
    }

    @Transient
    public Map<Long, Node> getUsedNodes() {
        final Map<Long, Node> nodes = new HashMap<>();
        for (final ExperimentNodeGroup experimentNodeGroup : getNodeGroups()) {
            for (final Node node : experimentNodeGroup.getNodes()) {
                nodes.put(node.getId(), node);
            }
        }
        return nodes;
    }

    @Transient
    public Experiment createCopy() {
        final Experiment experiment = new Experiment();
        experiment.setName(getName());
        experiment.setDescription(getDescription());
        experiment.setState(ExperimentState.SCHEDULED);
        experiment.setStartTime(getStartTime());
        experiment.setEndTime(getEndTime());
        experiment.setReplications(getReplications());
        experiment.setPauseBetweenReplications(getPauseBetweenReplications());
        experiment.setSampleInterval(getSampleInterval());
        experiment.setRestartNodes(isRestartNodes());
        experiment.setLockTestbed(isLockTestbed());
        experiment.setInteractive(isInteractive());
        experiment.setTags(getTags());

        getNodeGroups()
                .stream()
                .map((experimentNodeGroup) -> experimentNodeGroup.createCopy(experiment))
                .forEachOrdered(experiment.getNodeGroups()::add);
        getFiles()
                .stream()
                .map((experimentFile) -> experimentFile.createCopy(experiment))
                .forEachOrdered(experiment.getFiles()::add);
        getVariables()
                .stream()
                .map((variable) -> variable.createCopy(experiment))
                .forEachOrdered(experiment.getVariables()::add);
        getActionBlocks().stream().map(actionBlock -> actionBlock.createCopy(experiment))
                         .forEachOrdered(experiment.getActionBlocks()::add);
        return experiment;
    }
}
