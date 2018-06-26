package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Created by matthias on 29.01.16.
 */
@MappedSuperclass
public abstract class UniquelyNamedEntity extends GeneratedIdEntity {

    private String name;

    @Basic
    @Column(unique = true)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("name", name).toString();
    }
}
