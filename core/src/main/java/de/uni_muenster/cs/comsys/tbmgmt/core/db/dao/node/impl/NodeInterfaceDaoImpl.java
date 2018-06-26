package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeInterfaceDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import org.springframework.stereotype.Repository;

import java.util.logging.Logger;

@Repository("nodeInterfaceDao")
public class NodeInterfaceDaoImpl extends DaoImpl<NodeInterface, Long> implements NodeInterfaceDao {

    private static final Logger LOG = Logger.getLogger(NodeInterfaceDaoImpl.class.getName());

    public NodeInterfaceDaoImpl() {
        super(NodeInterface.class);
    }
}
