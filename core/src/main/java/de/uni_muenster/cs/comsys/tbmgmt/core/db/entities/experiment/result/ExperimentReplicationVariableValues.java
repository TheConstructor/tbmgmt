package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.JsonbUserType;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthias on 26.02.16.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"experimentReplicationResult_id", "sequence"})})
public class ExperimentReplicationVariableValues extends GeneratedIdEntity {
    private ExperimentReplicationResult experimentReplicationResult;
    private long sequence;
    private Map<String, String> variableValues = new HashMap<>();

    @ManyToOne(optional = false)
    public ExperimentReplicationResult getExperimentReplicationResult() {
        return experimentReplicationResult;
    }

    public void setExperimentReplicationResult(final ExperimentReplicationResult experimentReplicationResult) {
        this.experimentReplicationResult = experimentReplicationResult;
    }

    @Basic
    @Column(nullable = false)
    public long getSequence() {
        return sequence;
    }

    public void setSequence(final long sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Type(type = JsonbUserType.TYPE_STRING)
    @Column(columnDefinition = JsonbUserType.PG_TYPE_STRING)
    public Map<String, String> getVariableValues() {
        return variableValues;
    }

    public void setVariableValues(final Map<String, String> parameterValues) {
        this.variableValues = parameterValues;
    }
}
