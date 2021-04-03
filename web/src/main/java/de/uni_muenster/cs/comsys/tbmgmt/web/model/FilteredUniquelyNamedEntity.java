package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.UniquelyNamedEntity;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * Created by matthias on 25.02.16.
 */
public class FilteredUniquelyNamedEntity<E extends UniquelyNamedEntity> implements Serializable {

    private final E entity;

    public FilteredUniquelyNamedEntity(final E entity) {
        this.entity = entity;
    }

    public Long getId() {
        return entity.getId();
    }

    @NotBlank
    public String getName() {
        return entity.getName();
    }

    public void setName(final String name) {
        entity.setName(name);
    }
}
