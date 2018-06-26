package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import org.hibernate.jpa.QueryHints;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface Dao<Entity, PrimaryKey extends Serializable> {
    Map<String, Object> VIEW_QUERY_HINTS = getViewQueryHints();

    static Map<String, Object> getViewQueryHints() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(QueryHints.HINT_READONLY, true);
        return Collections.unmodifiableMap(map);
    }

    EntityManager getEntityManager();

    Class<Entity> getEntityClass();

    TransactionTemplate getTransactionTemplate();

    CriteriaBuilder getCriteriaBuilder();

    Entity find(PrimaryKey primaryKey);

    Entity merge(Entity entity);

    /**
     * This is mainly a support-method for web-flows
     */
    Entity mergeWithTransaction(Entity entity);

    void persist(Entity entity);

    void persistWithSideTransaction(Entity entity);

    void remove(Entity entity);

    Long entityCount();

    int validPage(int page, int pageCount);

    <T> T getSingleResult(CriteriaQuery<T> query);

    <T> List<T> getResultList(CriteriaQuery<T> query, int first, int maxResults, Map<String, Object> hints);

    <T> List<T> getResultList(CriteriaQuery<T> query, int first, int maxResults);

    <T> List<T> getResultList(CriteriaQuery<T> query);

    <T> List<T> getResultList(CriteriaQuery<T> query, Map<String, Object> hints);
}
