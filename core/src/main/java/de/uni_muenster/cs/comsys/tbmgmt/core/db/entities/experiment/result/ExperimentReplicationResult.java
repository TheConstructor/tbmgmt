package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by matthias on 05.12.15.
 */
@Entity
public class ExperimentReplicationResult extends GeneratedIdEntity {
    private Experiment experiment;
    private long sequence;

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Basic
    @Column(nullable = false)
    public long getSequence() {
        return sequence;
    }

    public void setSequence(final long sequence) {
        this.sequence = sequence;
    }
}
