package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.FileEntity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"experiment_id", "fileName"})})
public class ExperimentFile extends FileEntity {

    private static final Logger LOG = Logger.getLogger(ExperimentFile.class.getName());

    private Experiment experiment;
    private boolean eval;

    @Override
    @Transient
    protected Path getStoragePath() {
        return fileConfig.getExperimentFileStoragePath();
    }

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Basic
    public boolean isEval() {
        return eval;
    }

    public void setEval(final boolean eval) {
        this.eval = eval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("eval", eval).toString();
    }

    @Transient
    public ExperimentFile createCopy(final Experiment experiment) {
        final ExperimentFile experimentFile = new ExperimentFile();
        experimentFile.setExperiment(experiment);
        copyTo(experimentFile);
        experimentFile.setEval(isEval());
        return experimentFile;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExperimentFile)) {
            return false;
        }

        final ExperimentFile that = (ExperimentFile) o;
        return new EqualsBuilder()
                .append(getExperiment(), that.getExperiment())
                .append(getFileName(), that.getFileName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getExperiment()).append(getFileName()).toHashCode();
    }
}
