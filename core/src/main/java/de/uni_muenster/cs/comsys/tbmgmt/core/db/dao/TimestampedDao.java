package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.TimestampedEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.TimestampedEntity_;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by matthias on 29.12.15.
 */
public interface TimestampedDao<Entity extends TimestampedEntity, PrimaryKey extends Serializable>
        extends Dao<Entity, PrimaryKey> {

    default long entitiesUpdatedSince(final Instant time) {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        final Root<Entity> root = query.from(getEntityClass());
        query.select(criteriaBuilder.countDistinct(root));
        query.where(criteriaBuilder.greaterThanOrEqualTo(root.get(TimestampedEntity_.updated), time));
        return getSingleResult(query);
    }
}
