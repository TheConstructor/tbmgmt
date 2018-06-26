package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity_;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Created by matthias on 29.12.15.
 */
public interface GeneratedIdDao<Entity extends GeneratedIdEntity> extends SingularPrimaryKeyDao<Entity, Long> {

    @Override
    default SingularAttribute<GeneratedIdEntity, Long> getIdColumn() {
        return GeneratedIdEntity_.id;
    }
}
