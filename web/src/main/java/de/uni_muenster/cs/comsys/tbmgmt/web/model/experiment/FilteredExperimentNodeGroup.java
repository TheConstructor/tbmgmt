package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NodeNameResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.NodeRole;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.ConvertingList;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Created by matthias on 23.04.15.
 */
@Configurable
public class FilteredExperimentNodeGroup implements Serializable, Validateable {
    private final     Experiment          experiment;
    private final     ExperimentNodeGroup experimentNodeGroup;
    @Autowired
    private transient NodeNameResolver    nodeNameResolver;

    public FilteredExperimentNodeGroup(final Experiment experiment,
            final ExperimentNodeGroup experimentNodeGroup) {
        this.experiment = experiment;
        this.experimentNodeGroup = experimentNodeGroup;
    }

    @NotNull
    @NotBlank
    public String getName() {
        return experimentNodeGroup.getName();
    }

    public void setName(final String name) {
        experimentNodeGroup.setName(name);
    }

    public NodeRole getRole() {
        return experimentNodeGroup.getRole();
    }

    public void setRole(final NodeRole role) {
        experimentNodeGroup.setRole(role);
    }

    @NotNull
    @NotEmpty
    public List<String> getNodes() {
        final List<Node> nodes = experimentNodeGroup.getNodes();
        if (nodes == null) {
            experimentNodeGroup.setNodes(new ArrayList<>());
        }
        return new ConvertingList<>(nodes, Node::getName, nodeNameResolver::getNodeByName);
    }

    public void setNodes(final List<String> nodes) {
        if (nodes == null) {
            experimentNodeGroup.setNodes(new ArrayList<>());
        } else {
            experimentNodeGroup
                    .setNodes(nodes.stream().map(nodeNameResolver::getNodeByName)
                            .collect(Collectors.toCollection(ArrayList::new)));
        }
    }

    public boolean isInUse() {
        return experiment.isNodeGroupInUse(experimentNodeGroup);
    }

    @Override
    public String toString() {
        return experimentNodeGroup.toString();
    }

    @Override
    public void validate(final ValidationContext validationContext) {
        final MessageContext messageContext = validationContext.getMessageContext();
        for (final ExperimentNodeGroup nodeGroup : experiment.getNodeGroups()) {
            //noinspection ObjectEquality
            if (Objects.equals(nodeGroup.getName(), experimentNodeGroup.getName())
                    && nodeGroup != experimentNodeGroup) {
                messageContext.addMessage(
                        new MessageBuilder().error().source("name").defaultText("Node Group-names need to be unique")
                                .build());
                break;
            }
        }
    }
}
