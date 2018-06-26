package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.nio.file.Path;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "fileName")})
public class EvaluationScript extends FileEntity {
    @Override
    @Transient
    protected Path getStoragePath() {
        return fileConfig.getEvaluationScriptStoragePath();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Transient
    public EvaluationScript createCopy() {
        final EvaluationScript evaluationScript = new EvaluationScript();
        copyTo(evaluationScript);
        return evaluationScript;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EvaluationScript)) {
            return false;
        }

        final EvaluationScript that = (EvaluationScript) o;
        return new EqualsBuilder()
                .append(getFileName(), that.getFileName())
                .append(getFile(), that.getFile())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getFileName()).append(getFile()).toHashCode();
    }
}
