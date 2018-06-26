package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.Dao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentLogEntryDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogEntryCreator;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by matthias on 16.03.16.
 */
public class ActionResultStorageHelper {
    @Autowired
    private ExecutorService actionResultStoragePool;

    @Autowired
    private ExperimentLogEntryDao experimentLogEntryDao;

    public LogEntryCreator getLogEntryCreator(final Experiment experiment, final ExperimentAction action,
                                              final ExperimentReplicationResult replicationResult,
                                              final ExperimentReplicationVariableValues variableValues,
                                              final ExperimentActionExecution actionExecution, final Node node) {
        return (logReason, line) -> {
            final LogLevel logLevel = LogEntryCreator.getLogLevel(logReason);
            final String message = LogEntryCreator.prependMessage(logReason, line);
            final ExperimentLogEntry experimentLogEntry = new ExperimentLogEntry();
            experimentLogEntry.setExperiment(experiment);
            experimentLogEntry.setAction(action);
            experimentLogEntry.setReplicationResult(replicationResult);
            experimentLogEntry.setVariableValues(variableValues);
            experimentLogEntry.setExperimentActionExecution(actionExecution);
            experimentLogEntry.setNode(node);
            experimentLogEntry.setLogLevel(logLevel);
            experimentLogEntry.setMessage(message);
            actionResultStoragePool.submit(() -> experimentLogEntryDao.persistWithSideTransaction(experimentLogEntry));
        };
    }

    public <T> Future<T> submit(final Callable<T> task) {
        return actionResultStoragePool.submit(task);
    }

    public <T> T submitAndWait(final Callable<T> task, final String message)
            throws InterruptedIOException, InterruptedException {
        return TaskResultHelper.getResult(submit(task), message);
    }

    public <T> Future<T> submit(final Runnable task, final T result) {
        return actionResultStoragePool.submit(task, result);
    }

    public <T> T submitAndWait(final Runnable task, final T result, final String message)
            throws InterruptedIOException, InterruptedException {
        return TaskResultHelper.getResult(submit(task, result), message);
    }

    public Future<?> submit(final Runnable task) {
        return actionResultStoragePool.submit(task);
    }

    public Object submitAndWait(final Runnable task, final String message)
            throws InterruptedIOException, InterruptedException {
        return TaskResultHelper.getResult(submit(task), message);
    }

    public <T> Future<T> persist(final Dao<? super T, ?> dao, final T entity) {
        return submit(() -> {
            dao.persistWithSideTransaction(entity);
            return entity;
        });
    }

    public <T> T persistAndWait(final Dao<? super T, ?> dao, final T entity, final String message)
            throws InterruptedIOException, InterruptedException {
        return TaskResultHelper.getResult(persist(dao, entity), message);
    }

    public <T> Future<T> merge(final Dao<T, ?> dao, final T entity) {
        return submit(() -> dao.mergeWithTransaction(entity));
    }

    public <T> T mergeAndWait(final Dao<T, ?> dao, final T entity, final String message)
            throws InterruptedIOException, InterruptedException {
        return TaskResultHelper.getResult(merge(dao, entity), message);
    }
}
