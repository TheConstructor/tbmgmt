package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import javax.persistence.metamodel.SingularAttribute;
import java.io.Serializable;

/**
 * Created by matthias on 29.12.15.
 */
public interface SingularPrimaryKeyDao<Entity, PrimaryKey extends Serializable> extends Dao<Entity, PrimaryKey> {

    SingularAttribute<? super Entity, PrimaryKey> getIdColumn();
}
