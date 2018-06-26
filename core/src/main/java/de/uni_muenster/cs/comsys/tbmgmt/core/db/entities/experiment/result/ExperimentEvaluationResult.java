package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.JsonbUserType;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by matthias on 09.01.16.
 */
@Entity
public class ExperimentEvaluationResult extends GeneratedIdEntity {
    private ExperimentActionExecution actionExecution;
    private JsonNode data;

    @ManyToOne(optional = false)
    public ExperimentActionExecution getActionExecution() {
        return actionExecution;
    }

    public void setActionExecution(final ExperimentActionExecution actionExecution) {
        this.actionExecution = actionExecution;
    }

    @Basic
    @Type(type = JsonbUserType.TYPE_STRING)
    @Column(columnDefinition = JsonbUserType.PG_TYPE_STRING)
    public JsonNode getData() {
        return data;
    }

    public void setData(final JsonNode data) {
        this.data = data;
    }
}
