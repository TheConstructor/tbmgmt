package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExecutionMode;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
public class ExperimentActionBlock extends GeneratedIdEntity {
    private Experiment experiment;
    private BigInteger sequence;
    private ExecutionMode executionMode;
    private List<ExperimentAction> actions = new ArrayList<>();

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Basic
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Enumerated(value = EnumType.STRING)
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(final ExecutionMode executionMode) {
        this.executionMode = executionMode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("sequence", sequence)
                                        .append("executionMode", executionMode)
                                        .append("actions", actions)
                                        .toString();
    }

    @Transient
    public boolean isNodeGroupInUse(final ExperimentNodeGroup experimentNodeGroup) {
        for (final ExperimentAction action : getActions()) {
            if (Objects.equals(action.getTargetedNodeGroup(), experimentNodeGroup)) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(mappedBy = "experimentActionBlock", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy(value = "sequence asc")
    public List<ExperimentAction> getActions() {
        return actions;
    }

    public void setActions(final List<ExperimentAction> actions) {
        this.actions = actions;
    }

    @Transient
    public void generateActionSequence() {
        BigInteger current = BigInteger.ZERO;
        for (final ExperimentAction action : getActions()) {
            action.setSequence(current);
            current = current.add(BigInteger.ONE);
        }
    }

    @Transient
    public ExperimentActionBlock createCopy(final Experiment experiment) {
        final ExperimentActionBlock experimentActionBlock = new ExperimentActionBlock();
        experimentActionBlock.setExperiment(experiment);
        experimentActionBlock.setSequence(getSequence());
        experimentActionBlock.setExecutionMode(getExecutionMode());
        getActions().stream().map(a -> a.createCopy(experimentActionBlock, experiment::getNodeGroupByName))
                .forEachOrdered(experimentActionBlock.getActions()::add);
        return experimentActionBlock;
    }
}
