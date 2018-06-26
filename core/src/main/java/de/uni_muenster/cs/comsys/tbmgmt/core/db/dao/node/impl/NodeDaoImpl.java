package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

/**
 Created by matthias on 16.03.15.
 */
@Repository("nodeDao")
public class NodeDaoImpl extends DaoImpl<Node, Long> implements NodeDao {

    private static final Logger LOG = Logger.getLogger(NodeDaoImpl.class.getName());

    public NodeDaoImpl() {
        super(Node.class);
    }

    @Override
    @Transactional
    public Node getNodeByName(final String name) {
        return getSingleResultByAttributeValue(Node_.name, name);
    }

    @Override
    public List<Node> getAllActiveNodesOrderedByName() {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<Node> query = criteriaBuilder.createQuery(Node.class);
        final Root<Node> nodeRoot = query.from(Node.class);
        query.orderBy(criteriaBuilder.asc(nodeRoot.get(Node_.name)));
        return getResultList(query);
    }

    @Override
    public List<Node> getAllActiveNodesWithInterfaces() {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<Node> query = criteriaBuilder.createQuery(Node.class);
        final Root<Node> nodeRoot = query.from(Node.class);
        query.distinct(true);
        nodeRoot.fetch(Node_.interfaces, JoinType.LEFT);
        query.orderBy(criteriaBuilder.asc(nodeRoot.get(Node_.name)));
        return getResultList(query);
    }

    @Override
    public Long countUsages(final Node node) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<ExperimentNodeGroup> nodeGroupRoot = query.from(ExperimentNodeGroup.class);
        query.select(cb.count(nodeGroupRoot));
        query.where(cb.isMember(node, nodeGroupRoot.get(ExperimentNodeGroup_.nodes)));
        return getSingleResult(query);
    }
}
