package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository("nodeTypeDao")
public class NodeTypeDaoImpl extends DaoImpl<NodeType, Long> implements NodeTypeDao {

    private static final Logger LOG = Logger.getLogger(NodeTypeDaoImpl.class.getName());

    public NodeTypeDaoImpl() {
        super(NodeType.class);
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("UnusedParameters") final ContextRefreshedEvent event) {
        final Long count = entityCount();
        LOG.log(Level.INFO, "Init found {0} node types", count);
        if (count == null || count <= 0) {
            for (final String typeName : new String[]{"router", "virtual"}) {
                getTransactionTemplate().execute((TransactionStatus status) -> {
                    final NodeType nodeType = new NodeType();
                    nodeType.setName(typeName);

                    return merge(nodeType);
                });
            }
        }
    }

    @Override
    public NodeType getByName(final String name) {
        return getSingleResultByAttributeValue(NodeType_.name, name);
    }

    @Override
    public List<String> getNames() {
        return getAllAttributeValues(NodeType_.name, String.class);
    }

    @Override
    public Long countUsages(final NodeType nodeType) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Node> nodeRoot = query.from(Node.class);
        query.select(cb.count(nodeRoot));
        query.where(cb.equal(nodeRoot.get(Node_.type), nodeType));
        return getSingleResult(query);
    }
}
