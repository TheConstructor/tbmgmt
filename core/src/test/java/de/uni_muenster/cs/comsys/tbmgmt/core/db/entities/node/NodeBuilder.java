package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by matthias on 29.02.16.
 */
public class NodeBuilder {
    private final Node node;

    public NodeBuilder() {
        node = new Node();
    }

    public NodeBuilder name(final String name) {
        node.setName(name);
        return this;
    }

    public NodeBuilder nodeInterfaces(final List<NodeInterface> interfaces) {
        node.setInterfaces(interfaces);
        if (interfaces != null) {
            for (final NodeInterface nodeInterface : interfaces) {
                nodeInterface.setNode(node);
            }
        }
        return this;
    }

    public NodeBuilder nodeInterfaces(final NodeInterface... interfaces) {
        return nodeInterfaces(interfaces == null ? null : new ArrayList<>(Arrays.asList(interfaces)));
    }

    public NodeBuilder nodeInterface(final NodeInterface nodeInterface) {
        if (node.getInterfaces() == null) {
            node.setInterfaces(new ArrayList<>());
        }
        node.getInterfaces().add(nodeInterface);
        nodeInterface.setNode(node);
        return this;
    }

    public NodeBuilder type(final NodeType type) {
        node.setType(type);
        return this;
    }

    public NodeBuilder testbed(final Testbed testbed) {
        node.setTestbed(testbed);
        return this;
    }

    public Node build() {
        return node;
    }
}
