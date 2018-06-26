package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.IdleExperiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.IdleExperiment_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExperimentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 Created by matthias on 16.03.15.
 */
@Repository("experimentDao")
public class ExperimentDaoImpl extends DaoImpl<Experiment, Long> implements ExperimentDao {

    private static final Logger LOG = Logger.getLogger(ExperimentDaoImpl.class.getName());

    @Autowired
    private TransactionTemplate instantWriteTransactionTemplate;

    public ExperimentDaoImpl() {
        super(Experiment.class);
    }

    @Override
    public List<Experiment> getExperimentsScheduledToRunBefore(final Instant before) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Experiment> cq = cb.createQuery(getEntityClass());
        final Root<Experiment> experimentPath = cq.from(getEntityClass());

        final Path<ExperimentState> experimentStatePath = experimentPath.get(Experiment_.state);
        cq.where(cb.or(
                // Scheduled and about to start
                cb.and(experimentStatePath.in(ExperimentState.SCHEDULED),
                        cb.lessThan(experimentPath.get(Experiment_.startTime), before)),
                // Cancelled - anytime
                experimentStatePath.in(ExperimentState.CANCELATION_REQUESTED)));

        return getResultList(cq);
    }

    @Override
    public List<Experiment> getExperimentsScheduledToCeaseBefore(final Instant before) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Experiment> cq = cb.createQuery(getEntityClass());
        final Root<Experiment> experimentPath = cq.from(getEntityClass());

        cq.where(cb.and(experimentPath.get(Experiment_.state).in(ExperimentState.END_STATES).not(),
                cb.lessThan(experimentPath.get(Experiment_.endTime), before)));

        return getResultList(cq);
    }

    @Override
    public ExperimentState immediatelySetState(final Experiment experiment, final ExperimentState state) {
        final ExperimentState previousState = instantWriteTransactionTemplate.execute(transactionStatus -> {
            final Experiment temporaryExperiment = find(experiment.getId());
            final ExperimentState previous = temporaryExperiment.getState();
            temporaryExperiment.setState(state);
            return previous;
        });
        getEntityManager().refresh(experiment);
        return previousState;
    }

    @Override
    public boolean immediatelySetStateIfNotEnded(final Experiment experiment, final ExperimentState state) {
        final boolean stateChanged = instantWriteTransactionTemplate.execute(transactionStatus -> {
            final Experiment temporaryExperiment = find(experiment.getId());
            final ExperimentState previous = temporaryExperiment.getState();
            if (!Objects.equals(previous, state) && !previous.isEndState()) {
                temporaryExperiment.setState(state);
                return true;
            } else {
                // We do not want to commit something...
                transactionStatus.setRollbackOnly();
                return false;
            }
        });
        getEntityManager().refresh(experiment);
        return stateChanged;
    }

    @Override
    public List<Experiment> getConflictingExperiments(final Experiment experiment) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Experiment> cq = cb.createQuery(getEntityClass());
        final Root<Experiment> experimentPath = cq.from(getEntityClass());
        final ListJoin<Experiment, ExperimentNodeGroup> nodeGroupsPath =
                experimentPath.join(Experiment_.nodeGroups, JoinType.LEFT);
        final ListJoin<ExperimentNodeGroup, Node> nodesPath =
                nodeGroupsPath.join(ExperimentNodeGroup_.nodes, JoinType.LEFT);

        final Predicate otherExperiment = cb.notEqual(experimentPath.get(Experiment_.id), experiment.getId());
        final Predicate experimentInBlockingState =
                experimentPath.get(Experiment_.state).in(ExperimentState.getBlockingStates());

        if (experiment.isLockTestbed()) {
            // All other experiments block us, as we lock the testbed
            cq.where(cb.and(otherExperiment, experimentInBlockingState));
        } else {
            // Only experiments, which lock the testbed or share nodes block us
            final Predicate testdbedLocked = cb.equal(experimentPath.get(Experiment_.lockTestbed), true);
            final Predicate usingSameNodes = nodesPath.get(Node_.id).in(experiment.getUsedNodeIds());

            cq.where(cb.and(otherExperiment, experimentInBlockingState, cb.or(testdbedLocked, usingSameNodes)));
        }

        return getResultList(cq);
    }

    @Override
    public List<Experiment> getPossiblyConflictingExperiments(final Long id, final Instant startTime,
                                                              final Duration duration, final Set<Long> nodeIds) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Experiment> cq = cb.createQuery(getEntityClass());
        final Root<Experiment> experimentPath = cq.from(getEntityClass());
        final ListJoin<Experiment, ExperimentNodeGroup> nodeGroupsPath =
                experimentPath.join(Experiment_.nodeGroups, JoinType.LEFT);
        final ListJoin<ExperimentNodeGroup, Node> nodesPath =
                nodeGroupsPath.join(ExperimentNodeGroup_.nodes, JoinType.LEFT);

        final List<Predicate> otherExperimentSelection = new ArrayList<>(5);
        // If we know our id we can exclude us from searches
        if (id != null) {
            otherExperimentSelection.add(cb.notEqual(experimentPath.get(Experiment_.id), id));
        }
        // Experiment is not already done
        otherExperimentSelection.add(experimentPath.get(Experiment_.state).in(ExperimentState.getEndStates()).not());

        // other's start-time before our end
        otherExperimentSelection.add(cb.lessThan(experimentPath.get(Experiment_.startTime), startTime.plus(duration)));
        // other's end-time after our start
        otherExperimentSelection.add(cb.greaterThan(experimentPath.get(Experiment_.endTime), startTime));

        // and a lock on the testbed or a node in common
        final List<Predicate> blockingSelection = new ArrayList<>(2);
        blockingSelection.add(cb.equal(experimentPath.get(Experiment_.lockTestbed), true));
        if (nodeIds != null && !nodeIds.isEmpty()) {
            blockingSelection.add(nodesPath.get(Node_.id).in(nodeIds));
        }
        otherExperimentSelection.add(cb.or(blockingSelection.toArray(new Predicate[blockingSelection.size()])));

        cq.where(cb.and(otherExperimentSelection.toArray(new Predicate[otherExperimentSelection.size()])));

        return getResultList(cq);
    }

    @Override
    public void remove(final Experiment experiment) {
        // We need to manually take care of these as there are no collections defined which make them navigable
        // from Experiment. Otherwise we would get foreign-key-constraint violations.
        final CriteriaBuilder cb = getCriteriaBuilder();
        final EntityManager entityManager = getEntityManager();
        {
            final CriteriaDelete<ExperimentLogEntry> query = cb.createCriteriaDelete(ExperimentLogEntry.class);
            final Root<ExperimentLogEntry> logEntryRoot = query.from(ExperimentLogEntry.class);
            query.where(cb.equal(logEntryRoot.get(ExperimentLogEntry_.experiment), experiment));
            entityManager.createQuery(query).executeUpdate();
        }
        {
            final CriteriaQuery<Long> query = cb.createQuery(Long.class);
            final Root<ExperimentEvaluationResult> evaluationResultRoot = query.from(ExperimentEvaluationResult.class);
            query.distinct(true);
            query.select(evaluationResultRoot.get(GeneratedIdEntity_.id));
            query.where(cb.equal(evaluationResultRoot
                    .get(ExperimentEvaluationResult_.actionExecution)
                    .get(ExperimentActionExecution_.variableValues)
                    .get(ExperimentReplicationVariableValues_.experimentReplicationResult)
                    .get(ExperimentReplicationResult_.experiment), experiment));
            final List<Long> ids = getResultList(query);

            removeByIDs(cb, entityManager, ExperimentEvaluationResult.class, ids);
        }
        {
            final CriteriaQuery<Long> query = cb.createQuery(Long.class);
            final Root<ExperimentActionExecution> actionExecutionRoot = query.from(ExperimentActionExecution.class);
            query.distinct(true);
            query.select(actionExecutionRoot.get(GeneratedIdEntity_.id));
            query.where(cb.equal(actionExecutionRoot
                    .get(ExperimentActionExecution_.variableValues)
                    .get(ExperimentReplicationVariableValues_.experimentReplicationResult)
                    .get(ExperimentReplicationResult_.experiment), experiment));
            final List<Long> ids = getResultList(query);

            removeByIDs(cb, entityManager, ExperimentActionExecution.class, ids);
        }
        {
            final CriteriaQuery<Long> query = cb.createQuery(Long.class);
            final Root<ExperimentReplicationVariableValues> replicationVariableValuesRoot =
                    query.from(ExperimentReplicationVariableValues.class);
            query.distinct(true);
            query.select(replicationVariableValuesRoot.get(GeneratedIdEntity_.id));
            query.where(cb.equal(replicationVariableValuesRoot
                    .get(ExperimentReplicationVariableValues_.experimentReplicationResult)
                    .get(ExperimentReplicationResult_.experiment), experiment));
            final List<Long> ids = getResultList(query);

            removeByIDs(cb, entityManager, ExperimentReplicationVariableValues.class, ids);
        }
        {
            final CriteriaDelete<ExperimentReplicationResult> query =
                    cb.createCriteriaDelete(ExperimentReplicationResult.class);
            final Root<ExperimentReplicationResult> replicationResultRoot =
                    query.from(ExperimentReplicationResult.class);
            query.where(cb.equal(replicationResultRoot.get(ExperimentReplicationResult_.experiment), experiment));
            entityManager.createQuery(query).executeUpdate();
        }
        {
            final CriteriaDelete<IdleExperiment> query = cb.createCriteriaDelete(IdleExperiment.class);
            final Root<IdleExperiment> idleExperimentRoot = query.from(IdleExperiment.class);
            query.where(cb.equal(idleExperimentRoot.get(IdleExperiment_.experiment), experiment));
            entityManager.createQuery(query).executeUpdate();
        }
        super.remove(experiment);
    }

    private <T extends GeneratedIdEntity> void removeByIDs(final CriteriaBuilder cb, final EntityManager entityManager,
                                                           final Class<T> targetEntity, final List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        final CriteriaDelete<T> deleteQuery = cb.createCriteriaDelete(targetEntity);
        final Root<T> root = deleteQuery.from(targetEntity);
        deleteQuery.where(root.get(GeneratedIdEntity_.id).in(ids));
        entityManager.createQuery(deleteQuery).executeUpdate();
    }
}
