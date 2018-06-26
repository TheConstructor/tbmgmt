package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by matthias on 15.02.2015.
 */
@MappedSuperclass
public abstract class TimestampedEntity implements Serializable {
    private static final long serialVersionUID = 42L;

    private Instant created;
    private Instant updated;
    private long version;

    @Basic
    public Instant getCreated() {
        return created;
    }

    protected void setCreated(Instant created) {
        this.created = created;
    }

    @Basic
    public Instant getUpdated() {
        return updated;
    }

    protected void setUpdated(Instant modified) {
        this.updated = modified;
    }

    @Version
    public long getVersion() {
        return version;
    }

    protected void setVersion(long version) {
        this.version = version;
    }

    @PrePersist
    protected void onCreate() {
        updated = created = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = Instant.now();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("created", created)
                .append("updated", updated)
                .append("version", version)
                .toString();
    }
}
