package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType;

import java.util.List;

/**
 * Created by matthias on 16.03.15.
 */
public interface NodeTypeDao extends GeneratedIdDao<NodeType> {
    NodeType getByName(String name);

    List<String> getNames();

    Long countUsages(NodeType nodeType);
}
