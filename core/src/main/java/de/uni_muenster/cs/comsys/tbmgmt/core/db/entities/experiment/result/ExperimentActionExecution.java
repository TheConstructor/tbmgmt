package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.JsonbUserType;
import org.hibernate.annotations.Type;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matthias on 29.02.16.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"variableValues_id", "action_id", "node_id", "sequence"})})
public class ExperimentActionExecution extends GeneratedIdEntity {
    private ExperimentReplicationVariableValues variableValues;
    private ExperimentAction action;
    private Node node;
    private long sequence;
    private Map<String, String> nodeAddressValues = new HashMap<>();
    private Integer returnCode;
    /**
     * This gives the time right before the ssh-connection was established
     */
    private Instant startedAfter;
    /**
     * It depends on amount of unread data, etc how close this is to the time the command terminated
     */
    private Instant endedBefore;

    @ManyToOne(optional = false)
    public ExperimentReplicationVariableValues getVariableValues() {
        return variableValues;
    }

    public void setVariableValues(final ExperimentReplicationVariableValues variableValues) {
        this.variableValues = variableValues;
    }

    @ManyToOne(optional = false)
    public ExperimentAction getAction() {
        return action;
    }

    public void setAction(final ExperimentAction action) {
        this.action = action;
    }

    @ManyToOne(optional = false)
    public Node getNode() {
        return node;
    }

    public void setNode(final Node node) {
        this.node = node;
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
    public Map<String, String> getNodeAddressValues() {
        return nodeAddressValues;
    }

    public void setNodeAddressValues(final Map<String, String> nodeAddressValues) {
        this.nodeAddressValues = nodeAddressValues;
    }

    @Basic
    @Column(nullable = true)
    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(final Integer returnCode) {
        this.returnCode = returnCode;
    }

    @Basic
    @Column(nullable = false)
    public Instant getStartedAfter() {
        return startedAfter;
    }

    public void setStartedAfter(final Instant startedAfter) {
        this.startedAfter = startedAfter;
    }

    @Basic
    @Column(nullable = true)
    public Instant getEndedBefore() {
        return endedBefore;
    }

    public void setEndedBefore(final Instant endedBefore) {
        this.endedBefore = endedBefore;
    }
}
