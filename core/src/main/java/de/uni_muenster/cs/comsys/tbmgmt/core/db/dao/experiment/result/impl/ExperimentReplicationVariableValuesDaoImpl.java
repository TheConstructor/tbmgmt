package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationVariableValuesDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationResult;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues_;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

@Repository("experimentReplicationVariableValuesDao")
public class ExperimentReplicationVariableValuesDaoImpl extends DaoImpl<ExperimentReplicationVariableValues, Long>
        implements ExperimentReplicationVariableValuesDao {

    private static final Logger LOG = Logger.getLogger(ExperimentReplicationVariableValuesDaoImpl.class.getName());

    public ExperimentReplicationVariableValuesDaoImpl() {
        super(ExperimentReplicationVariableValues.class);
    }

    @Override
    public List<ExperimentReplicationVariableValues> findByReplication(final ExperimentReplicationResult replication) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<ExperimentReplicationVariableValues> query =
                cb.createQuery(ExperimentReplicationVariableValues.class);
        final Root<ExperimentReplicationVariableValues> replicationResultRoot =
                query.from(ExperimentReplicationVariableValues.class);
        query.where(
                cb.equal(replicationResultRoot.get(ExperimentReplicationVariableValues_.experimentReplicationResult),
                        replication));
        query.orderBy(cb.asc(replicationResultRoot.get(ExperimentReplicationVariableValues_.sequence)));
        return getResultList(query);
    }
}
