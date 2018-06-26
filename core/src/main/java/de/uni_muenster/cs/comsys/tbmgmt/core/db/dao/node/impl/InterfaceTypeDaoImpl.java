package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface_;
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

@Repository("interfaceTypeDao")
public class InterfaceTypeDaoImpl extends DaoImpl<InterfaceType, Long> implements InterfaceTypeDao {

    private static final Logger LOG = Logger.getLogger(InterfaceTypeDaoImpl.class.getName());

    public InterfaceTypeDaoImpl() {
        super(InterfaceType.class);
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("UnusedParameters") final ContextRefreshedEvent event) {
        Long count = entityCount();
        LOG.log(Level.INFO, "Init found {0} interface types", count);
        if (count == null || count <= 0) {
            for (String typeName : new String[]{"wired", "wireless", "virtual"}) {
                getTransactionTemplate().execute((TransactionStatus status) -> {
                    InterfaceType interfaceType = new InterfaceType();
                    interfaceType.setName(typeName);

                    return merge(interfaceType);
                });
            }
        }
    }

    @Override
    public InterfaceType getByName(String name) {
        return getSingleResultByAttributeValue(InterfaceType_.name, name);
    }

    @Override
    public List<String> getNames() {
        return getAllAttributeValues(InterfaceType_.name, String.class);
    }

    @Override
    public Long countUsages(final InterfaceType interfaceType) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<NodeInterface> nodeInterfaceRoot = query.from(NodeInterface.class);
        query.select(cb.count(nodeInterfaceRoot));
        query.where(cb.equal(nodeInterfaceRoot.get(NodeInterface_.type), interfaceType));
        return getSingleResult(query);
    }
}
