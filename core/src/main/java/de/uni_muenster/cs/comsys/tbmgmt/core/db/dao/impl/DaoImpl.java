package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.Dao;
import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by matthias on 15.02.2015.
 */
public abstract class DaoImpl<Entity, PrimaryKey extends Serializable> implements Dao<Entity, PrimaryKey> {
    private final Class<Entity> entityClass;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private TransactionTemplate instantWriteTransactionTemplate;
    @Autowired
    private DbConfiguration dbConfiguration;

    public DaoImpl(final Class<Entity> entityClass) {
        if (!entityClass.isAnnotationPresent(javax.persistence.Entity.class)) {
            throw new IllegalArgumentException(
                    entityClass + " is not annotated as @Entity and will not be found by hibernate");
        }
        this.entityClass = entityClass;
    }

    @Override
    public Class<Entity> getEntityClass() {
        return entityClass;
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    protected TransactionTemplate getInstantWriteTransactionTemplate() {
        return instantWriteTransactionTemplate;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    @Override
    public Entity find(final PrimaryKey primaryKey) {
        return getEntityManager().find(getEntityClass(), primaryKey);
    }

    @Override
    public Entity merge(final Entity entity) {
        return getEntityManager().merge(entity);
    }

    /**
     * This is mainly a support-method for web-flows
     */
    @Override
    public Entity mergeWithTransaction(final Entity entity) {
        return getTransactionTemplate().execute(transactionStatus -> this.merge(entity));
    }

    @Override
    public void persist(final Entity entity) {
        getEntityManager().persist(entity);
    }

    @Override
    public void persistWithSideTransaction(final Entity entity) {
        getInstantWriteTransactionTemplate().execute(transactionStatus -> {
            this.persist(entity);
            return null;
        });
    }

    @Override
    public void remove(final Entity entity) {
        getEntityManager().remove(entity);
    }

    @Override
    public Long entityCount() {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        final Root<Entity> root = query.from(getEntityClass());

        query.select(criteriaBuilder.count(root));
        return getSingleResult(query);
    }

    @Override
    public int validPage(final int page, final int pageCount) {
        return Math.max(0, Math.min(page, pageCount - 1));
    }

    @Override
    public <T> T getSingleResult(final CriteriaQuery<T> query) {
        final TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
        typedQuery.setHint(QueryHints.SPEC_HINT_TIMEOUT, dbConfiguration.getQueryTimeout());
        return typedQuery.getSingleResult();
    }

    @Override
    public <T> List<T> getResultList(final CriteriaQuery<T> query, final int first, final int maxResults,
                                     final Map<String, Object> hints) {
        final TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
        typedQuery.setFirstResult(first);
        typedQuery.setMaxResults(maxResults);
        typedQuery.setHint(QueryHints.SPEC_HINT_TIMEOUT, dbConfiguration.getQueryTimeout());
        for (final Map.Entry<String, Object> hint : hints.entrySet()) {
            typedQuery.setHint(hint.getKey(), hint.getValue());
        }
        return typedQuery.getResultList();
    }

    @Override
    public <T> List<T> getResultList(final CriteriaQuery<T> query, final int first, final int maxResults) {
        return getResultList(query, first, maxResults, Collections.emptyMap());
    }

    @Override
    public <T> List<T> getResultList(final CriteriaQuery<T> query) {
        return getResultList(query, Collections.emptyMap());
    }

    @Override
    public <T> List<T> getResultList(final CriteriaQuery<T> query, final Map<String, Object> hints) {
        final TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
        typedQuery.setHint(QueryHints.SPEC_HINT_TIMEOUT, dbConfiguration.getQueryTimeout());
        for (final Map.Entry<String, Object> hint : hints.entrySet()) {
            typedQuery.setHint(hint.getKey(), hint.getValue());
        }
        return typedQuery.getResultList();
    }

    protected <T> int executeUpdate(final CriteriaUpdate<T> criteriaUpdate) {
        final EntityManager entityManager = getEntityManager();
        final Query query = entityManager.createQuery(criteriaUpdate);
        query.setHint(QueryHints.SPEC_HINT_TIMEOUT, dbConfiguration.getQueryTimeout());
        return query.executeUpdate();
    }

    protected <V> Entity getSingleResultByAttributeValue(final SingularAttribute<? super Entity, V> attribute,
                                                         final V value) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Entity> query = cb.createQuery(getEntityClass());
        final Root<Entity> evaluationScriptRoot = query.from(getEntityClass());
        query.where(cb.equal(evaluationScriptRoot.get(attribute), value));
        return getSingleResult(query);
    }

    protected <V> List<V> getAllAttributeValues(final SingularAttribute<? super Entity, V> attribute,
                                                final Class<V> valueClass) {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<V> query = criteriaBuilder.createQuery(valueClass);
        final Root<Entity> root = query.from(getEntityClass());

        final Path<V> attributePath = root.get(attribute);
        query.select(attributePath);
        query.orderBy(criteriaBuilder.asc(attributePath));

        return getResultList(query);
    }
}
