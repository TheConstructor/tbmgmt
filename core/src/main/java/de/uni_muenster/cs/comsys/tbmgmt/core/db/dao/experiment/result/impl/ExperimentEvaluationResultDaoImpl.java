package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentEvaluationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentEvaluationResult_;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

@Repository("experimentEvaluationResultDao")
public class ExperimentEvaluationResultDaoImpl extends DaoImpl<ExperimentEvaluationResult, Long>
        implements ExperimentEvaluationResultDao {

    private static final Logger LOG = Logger.getLogger(ExperimentEvaluationResultDaoImpl.class.getName());

    public ExperimentEvaluationResultDaoImpl() {
        super(ExperimentEvaluationResult.class);
    }

    @Override
    public List<ExperimentEvaluationResult> findByActionExecution(final ExperimentActionExecution actionExecution) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<ExperimentEvaluationResult> query = cb.createQuery(ExperimentEvaluationResult.class);
        final Root<ExperimentEvaluationResult> evaluationResultRoot = query.from(ExperimentEvaluationResult.class);
        query.where(cb.equal(evaluationResultRoot.get(ExperimentEvaluationResult_.actionExecution), actionExecution));
        query.orderBy(cb.asc(evaluationResultRoot.get(ExperimentEvaluationResult_.id)));
        return getResultList(query);
    }
}
