package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.NodeRole;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthias on 10.03.15.
 */
@Entity
public class ExperimentNodeGroup extends GeneratedIdEntity {
    private Experiment experiment;
    private String name;
    private NodeRole role;
    private List<Node> nodes = new ArrayList<>();

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getRole())
                .append(getNodes())
                .toHashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExperimentNodeGroup)) {
            return false;
        }

        final ExperimentNodeGroup that = (ExperimentNodeGroup) o;

        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(getName(), that.getName())
                .append(getRole(), that.getRole())
                .append(getNodes(), that.getNodes())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("name", name)
                                        .append("role", role)
                                        .append("nodes", nodes)
                                        .toString();
    }

    @Basic
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Basic
    @Enumerated(value = EnumType.STRING)
    public NodeRole getRole() {
        return role;
    }

    public void setRole(final NodeRole role) {
        this.role = role;
    }

    @ManyToMany
    @JoinTable
    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(final List<Node> nodes) {
        this.nodes = nodes;
    }

    @Transient
    public ExperimentNodeGroup createCopy(final Experiment experiment) {
        final ExperimentNodeGroup experimentNodeGroup = new ExperimentNodeGroup();
        experimentNodeGroup.setExperiment(experiment);
        experimentNodeGroup.setName(getName());
        experimentNodeGroup.setRole(getRole());
        experimentNodeGroup.getNodes().addAll(getNodes());
        return experimentNodeGroup;
    }
}
