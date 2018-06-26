package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by matthias on 17.01.16.
 */
@Entity
public class IdleExperiment extends GeneratedIdEntity {
    private Experiment experiment;

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(final Experiment experiment) {
        this.experiment = experiment;
    }
}
