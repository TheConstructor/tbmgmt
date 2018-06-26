package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;

/**
 Created by matthias on 16.03.15.
 */
public interface NodeNameResolver {
    Node getNodeByName(String name);
}
