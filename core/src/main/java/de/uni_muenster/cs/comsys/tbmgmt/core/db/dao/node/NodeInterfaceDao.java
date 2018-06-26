package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TimestampedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;

/**
 * Created by matthias on 16.03.15.
 */
public interface NodeInterfaceDao extends GeneratedIdDao<NodeInterface>, TimestampedDao<NodeInterface, Long> {
}
