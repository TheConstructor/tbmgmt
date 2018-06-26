package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult_;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

@Repository("experimentReplicationResultDao")
public class ExperimentReplicationResultDaoImpl extends DaoImpl<ExperimentReplicationResult, Long>
        implements ExperimentReplicationResultDao {

    private static final Logger LOG = Logger.getLogger(ExperimentReplicationResultDaoImpl.class.getName());

    public ExperimentReplicationResultDaoImpl() {
        super(ExperimentReplicationResult.class);
    }

    @Override
    public List<ExperimentReplicationResult> findByExperiment(final Experiment experiment) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<ExperimentReplicationResult> query = cb.createQuery(ExperimentReplicationResult.class);
        final Root<ExperimentReplicationResult> replicationResultRoot = query.from(ExperimentReplicationResult.class);
        query.where(cb.equal(replicationResultRoot.get(ExperimentReplicationResult_.experiment), experiment));
        query.orderBy(cb.asc(replicationResultRoot.get(ExperimentReplicationResult_.created)));
        return getResultList(query);
    }
}
