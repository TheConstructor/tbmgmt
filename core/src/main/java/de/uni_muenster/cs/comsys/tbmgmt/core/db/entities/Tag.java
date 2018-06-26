package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;

/**
 * Created by matthias on 17.01.16.
 */
@Entity
public class Tag extends UniquelyNamedEntity implements Comparable<Tag> {

    @Override
    public int compareTo(final Tag o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Tag)) {
            return false;
        }

        final Tag tag1 = (Tag) o;

        return new EqualsBuilder().append(getName(), tag1.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getName()).toHashCode();
    }
}
