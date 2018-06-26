package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentActionExecutionDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node_;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@Repository("experimentActionExecutionDao")
public class ExperimentActionExecutionDaoImpl extends DaoImpl<ExperimentActionExecution, Long>
        implements ExperimentActionExecutionDao {

    private static final Logger LOG = Logger.getLogger(ExperimentActionExecutionDaoImpl.class.getName());

    public ExperimentActionExecutionDaoImpl() {
        super(ExperimentActionExecution.class);
    }

    @Override
    public int setReturnCode(final ExperimentActionExecution experimentActionExecution, final Integer returnCode) {
        return getInstantWriteTransactionTemplate().execute(status -> {
            final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
            final CriteriaUpdate<ExperimentActionExecution> query =
                    criteriaBuilder.createCriteriaUpdate(ExperimentActionExecution.class);
            final Root<ExperimentActionExecution> actionExecutionRoot = query.from(ExperimentActionExecution.class);
            query.set(actionExecutionRoot.get(ExperimentActionExecution_.returnCode), returnCode);
            query.where(
                    criteriaBuilder.equal(actionExecutionRoot.get(getIdColumn()), experimentActionExecution.getId()));
            return executeUpdate(query);
        });
    }

    @Override
    public int setEndedBefore(final ExperimentActionExecution experimentActionExecution, final Instant endedBefore) {
        return getInstantWriteTransactionTemplate().execute(status -> {
            final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
            final CriteriaUpdate<ExperimentActionExecution> query =
                    criteriaBuilder.createCriteriaUpdate(ExperimentActionExecution.class);
            final Root<ExperimentActionExecution> actionExecutionRoot = query.from(ExperimentActionExecution.class);
            query.set(actionExecutionRoot.get(ExperimentActionExecution_.endedBefore), endedBefore);
            query.where(
                    criteriaBuilder.equal(actionExecutionRoot.get(getIdColumn()), experimentActionExecution.getId()));
            return executeUpdate(query);
        });
    }

    @Override
    public List<ExperimentActionExecution> getFor(final ExperimentReplicationVariableValues variableValues,
                                                  final ExperimentAction action, final Node node) {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<ExperimentActionExecution> query =
                criteriaBuilder.createQuery(ExperimentActionExecution.class);
        final Root<ExperimentActionExecution> actionExecutionRoot = query.from(ExperimentActionExecution.class);
        query.where(criteriaBuilder.and(
                criteriaBuilder.equal(actionExecutionRoot.get(ExperimentActionExecution_.variableValues),
                        variableValues),
                criteriaBuilder.equal(actionExecutionRoot.get(ExperimentActionExecution_.action), action),
                criteriaBuilder.equal(actionExecutionRoot.get(ExperimentActionExecution_.node), node)));
        query.orderBy(criteriaBuilder.asc(actionExecutionRoot.get(ExperimentActionExecution_.sequence)));
        return getResultList(query);
    }

    @Override
    public List<ExperimentActionExecution> getFor(final ExperimentReplicationVariableValues variableValues,
                                                  final ExperimentAction action) {
        final CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        final CriteriaQuery<ExperimentActionExecution> query =
                criteriaBuilder.createQuery(ExperimentActionExecution.class);
        final Root<ExperimentActionExecution> actionExecutionRoot = query.from(ExperimentActionExecution.class);
        query.where(criteriaBuilder.and(
                criteriaBuilder.equal(actionExecutionRoot.get(ExperimentActionExecution_.variableValues),
                        variableValues),
                criteriaBuilder.equal(actionExecutionRoot.get(ExperimentActionExecution_.action), action)));
        query.orderBy(criteriaBuilder.asc(actionExecutionRoot.get(ExperimentActionExecution_.sequence)),
                criteriaBuilder.asc(actionExecutionRoot.get(ExperimentActionExecution_.node).get(Node_.name)));
        return getResultList(query);
    }
}
