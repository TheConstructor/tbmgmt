package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogLevel;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

/**
 * Created by matthias on 09.01.16.
 */
@Entity
public class ExperimentLogEntry extends GeneratedIdEntity {
    private Experiment experiment;
    private ExperimentAction action;
    private ExperimentReplicationResult replicationResult;
    private ExperimentReplicationVariableValues variableValues;
    private ExperimentActionExecution experimentActionExecution;
    private Node node;
    private LogLevel logLevel;
    private String message;

    @ManyToOne(optional = false)
    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    @ManyToOne(optional = true)
    public ExperimentAction getAction() {
        return action;
    }

    public void setAction(final ExperimentAction action) {
        this.action = action;
    }

    @ManyToOne(optional = true)
    public ExperimentReplicationResult getReplicationResult() {
        return replicationResult;
    }

    public void setReplicationResult(ExperimentReplicationResult replicationResult) {
        this.replicationResult = replicationResult;
    }

    @ManyToOne(optional = true)
    public ExperimentReplicationVariableValues getVariableValues() {
        return variableValues;
    }

    public void setVariableValues(final ExperimentReplicationVariableValues variableValues) {
        this.variableValues = variableValues;
    }

    @ManyToOne(optional = true)
    public ExperimentActionExecution getExperimentActionExecution() {
        return experimentActionExecution;
    }

    public void setExperimentActionExecution(ExperimentActionExecution experimentAction) {
        this.experimentActionExecution = experimentAction;
    }

    @ManyToOne(optional = true)
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Basic
    @Enumerated(EnumType.STRING)
    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Basic
    @Column(columnDefinition = "text")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
