package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 Created by matthias on 23.04.15.
 */
public class NameResolverMock implements NodeNameResolver {
    private final ConcurrentMap<String, Node> nodeMap = new ConcurrentHashMap<>();

    @Override
    public Node getNodeByName(final String name) {
        return nodeMap.computeIfAbsent(name, s -> {
            Node node = new Node();
            node.setName(s);
            return node;

        });
    }
}
