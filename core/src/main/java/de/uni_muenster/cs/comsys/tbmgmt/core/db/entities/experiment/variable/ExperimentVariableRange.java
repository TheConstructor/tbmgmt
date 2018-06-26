package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
public class ExperimentVariableRange extends GeneratedIdEntity {
    private ExperimentVariable experimentVariable;
    private String start;
    private String end;

    @OneToOne(optional = false)
    public ExperimentVariable getExperimentVariable() {
        return experimentVariable;
    }

    public void setExperimentVariable(final ExperimentVariable experimentVariable) {
        this.experimentVariable = experimentVariable;
    }

    @Basic
    public String getStart() {
        return start;
    }

    public void setStart(final String start) {
        this.start = start;
    }

    @Basic
    public String getEnd() {
        return end;
    }

    public void setEnd(final String end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("start", start)
                .append("end", end)
                .toString();
    }

    @Transient
    public ExperimentVariableRange createCopy(final ExperimentVariable experimentVariable) {
        final ExperimentVariableRange experimentVariableRange = new ExperimentVariableRange();
        experimentVariableRange.setExperimentVariable(experimentVariable);
        experimentVariableRange.setStart(getStart());
        experimentVariableRange.setEnd(getEnd());
        return experimentVariableRange;
    }
}
