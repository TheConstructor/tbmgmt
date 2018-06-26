package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.TestbedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Testbed;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Testbed_;
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

@Repository("testbedDao")
public class TestbedDaoImpl extends DaoImpl<Testbed, Long> implements TestbedDao {

    private static final Logger LOG = Logger.getLogger(TestbedDaoImpl.class.getName());

    public TestbedDaoImpl() {
        super(Testbed.class);
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("UnusedParameters") final ContextRefreshedEvent event) {
        final Long count = entityCount();
        LOG.log(Level.INFO, "Init found {0} testbeds", count);
        if (count == null || count <= 0) {
            for (final String testbedName : new String[]{"Testbed"}) {
                getTransactionTemplate().execute((TransactionStatus status) -> {
                    final Testbed testbed = new Testbed();
                    testbed.setName(testbedName);

                    return merge(testbed);
                });
            }
        }
    }

    @Override
    public Testbed getByName(final String name) {
        return getSingleResultByAttributeValue(Testbed_.name, name);
    }

    @Override
    public List<String> getNames() {
        return getAllAttributeValues(Testbed_.name, String.class);
    }

    @Override
    public Long countUsages(final Testbed testbed) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Node> nodeRoot = query.from(Node.class);
        query.select(cb.count(nodeRoot));
        query.where(cb.equal(nodeRoot.get(Node_.testbed), testbed));
        return getSingleResult(query);
    }
}
