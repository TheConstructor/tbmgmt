package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.math.BigInteger;

/**
 * Created by matthias on 14.03.15.
 */
@Entity
public class ExperimentVariableValue extends GeneratedIdEntity {
    private ExperimentVariable experimentVariable;
    private BigInteger sequence;
    private String value;

    @ManyToOne(optional = false)
    public ExperimentVariable getExperimentVariable() {
        return experimentVariable;
    }

    public void setExperimentVariable(final ExperimentVariable experimentVariable) {
        this.experimentVariable = experimentVariable;
    }

    @Basic
    public BigInteger getSequence() {
        return sequence;
    }

    public void setSequence(final BigInteger sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Column(length = 10000)
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("value", value)
                                        .toString();
    }

    @Transient
    public ExperimentVariableValue createCopy(final ExperimentVariable experimentVariable) {
        final ExperimentVariableValue experimentVariableValue = new ExperimentVariableValue();
        experimentVariableValue.setExperimentVariable(experimentVariable);
        experimentVariableValue.setValue(getValue());
        return experimentVariableValue;
    }
}
