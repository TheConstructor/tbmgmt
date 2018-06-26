package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentLogEntryDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogLevel;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by matthias on 04.12.15.
 */
@Configurable
public class ExperimentLogger extends Logger {
    private final Logger logger;
    private final Formatter formatter;
    private final Experiment experiment;
    private final ExperimentAction action;
    private final ExperimentReplicationResult replicationResult;
    private final ExperimentReplicationVariableValues variableValues;
    private final ExperimentActionExecution actionExecution;

    private final Node node;

    @Autowired
    private transient ExperimentLogEntryDao experimentLogEntryDao;

    @Autowired
    private transient ActionResultStorageHelper actionResultStorageHelper;

    public ExperimentLogger(final Logger logger, final Experiment experiment) {
        this(logger, experiment, null, null, null, null, null);
    }

    protected ExperimentLogger(final Logger logger, final Experiment experiment, final ExperimentAction action,
                               final ExperimentReplicationResult replicationResult,
                               final ExperimentReplicationVariableValues variableValues,
                               final ExperimentActionExecution actionExecution, final Node node) {
        super(logger.getName(), logger.getResourceBundleName());
        this.logger = logger;
        formatter = new Formatter() {
            @Override
            public String format(final LogRecord record) {
                return formatMessage(record);
            }
        };
        this.experiment = experiment;
        this.action = action;
        this.replicationResult = replicationResult;
        this.variableValues = variableValues;
        this.actionExecution = actionExecution;
        this.node = node;

        final ResourceBundle resourceBundle = logger.getResourceBundle();
        if (resourceBundle != null) {
            this.setResourceBundle(resourceBundle);
        }
    }

    @Override
    public boolean isLoggable(final Level level) {
        return true;
    }

    @Override
    public void log(final LogRecord record) {
        if (logger.isLoggable(record.getLevel())) {
            // If you experience problems with localization, remove the next line
            record.setMessage(getPrefixedMessage(record));
            logger.log(record);
        }
        final LogLevel logLevel = LogLevel.from(record.getLevel());
        final String message = formatter.format(record);
        final ExperimentLogEntry experimentLogEntry = new ExperimentLogEntry();
        experimentLogEntry.setExperiment(experiment);
        experimentLogEntry.setAction(action);
        experimentLogEntry.setReplicationResult(replicationResult);
        experimentLogEntry.setVariableValues(variableValues);
        experimentLogEntry.setExperimentActionExecution(actionExecution);
        experimentLogEntry.setNode(node);
        experimentLogEntry.setLogLevel(logLevel);
        experimentLogEntry.setMessage(message);
        actionResultStorageHelper.persist(experimentLogEntryDao, experimentLogEntry);
    }

    protected String getPrefixedMessage(final LogRecord record) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("experiment: ").append(experiment.getId());
        if (replicationResult != null) {
            stringBuilder.append(" replication: ").append(replicationResult.getId());
        }
        if (variableValues != null) {
            stringBuilder.append(" iteration: ").append(variableValues.getId());
        }
        if (action != null) {
            stringBuilder.append(" action: ").append(action.getId());
        }
        if (actionExecution != null) {
            stringBuilder.append(" invocation: ").append(actionExecution.getId());
        }
        stringBuilder.append(" -> ").append(record.getMessage());
        return stringBuilder.toString();
    }

    public ExperimentLogger withAction(final ExperimentAction action) {
        return new ExperimentLogger(logger, experiment, action, replicationResult, variableValues, actionExecution,
                node);
    }

    public ExperimentLogger withReplicationResult(final ExperimentReplicationResult replicationResult) {
        return new ExperimentLogger(logger, experiment, action, replicationResult, variableValues, actionExecution,
                node);
    }

    public ExperimentLogger withVariableValues(final ExperimentReplicationVariableValues variableValues) {
        return new ExperimentLogger(logger, experiment, action, replicationResult, variableValues, actionExecution,
                node);
    }

    public ExperimentLogger withActionExecution(final ExperimentActionExecution actionExecution) {
        return new ExperimentLogger(logger, experiment, action, replicationResult, variableValues, actionExecution,
                node);
    }

    public ExperimentLogger withNode(final Node node) {
        return new ExperimentLogger(logger, experiment, action, replicationResult, variableValues, actionExecution,
                node);
    }
}
