package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Created by matthias on 29.01.16.
 */
@MappedSuperclass
public abstract class GeneratedIdEntity extends TimestampedEntity {
    public static final String ID_SEQUENCE = "id_sequence";

    private Long id;

    @Id
    @GenericGenerator(name = GeneratedIdEntity.ID_SEQUENCE, strategy = "org.hibernate.id.enhanced"
            + ".SequenceStyleGenerator",
            parameters = {@Parameter(name = SequenceStyleGenerator.CONFIG_PREFER_SEQUENCE_PER_ENTITY, value = "true"),
                    @Parameter(name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, value = "_id")})
    @GeneratedValue(generator = GeneratedIdEntity.ID_SEQUENCE)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("id", id).toString();
    }
}
