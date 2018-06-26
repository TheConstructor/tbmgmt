package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TimestampedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NodeNameResolver;

import java.util.List;

/**
 Created by matthias on 16.03.15.
 */
public interface NodeDao extends GeneratedIdDao<Node>, TimestampedDao<Node, Long>, NodeNameResolver {
    List<Node> getAllActiveNodesOrderedByName();

    List<Node> getAllActiveNodesWithInterfaces();

    Long countUsages(Node node);
}
