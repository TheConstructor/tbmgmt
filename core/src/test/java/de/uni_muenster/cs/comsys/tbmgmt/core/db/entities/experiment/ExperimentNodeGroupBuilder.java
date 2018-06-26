package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.NodeRole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by matthias on 29.02.16.
 */
public class ExperimentNodeGroupBuilder {
    private final ExperimentNodeGroup nodeGroup;

    public ExperimentNodeGroupBuilder() {
        nodeGroup = new ExperimentNodeGroup();
    }

    public ExperimentNodeGroupBuilder experiment(final Experiment experiment) {
        nodeGroup.setExperiment(experiment);
        return this;
    }

    public ExperimentNodeGroupBuilder name(final String name) {
        nodeGroup.setName(name);
        return this;
    }

    public ExperimentNodeGroupBuilder role(final NodeRole role) {
        nodeGroup.setRole(role);
        return this;
    }

    public ExperimentNodeGroupBuilder nodes(final List<Node> nodes) {
        nodeGroup.setNodes(nodes);
        return this;
    }

    public ExperimentNodeGroupBuilder nodes(final Node... nodes) {
        nodeGroup.setNodes(nodes == null ? null : new ArrayList<>(Arrays.asList(nodes)));
        return this;
    }

    public ExperimentNodeGroupBuilder node(final Node node) {
        if (nodeGroup.getNodes() == null) {
            nodeGroup.setNodes(new ArrayList<>());
        }
        nodeGroup.getNodes().add(node);
        return this;
    }

    public ExperimentNodeGroup build() {
        return nodeGroup;
    }
}
