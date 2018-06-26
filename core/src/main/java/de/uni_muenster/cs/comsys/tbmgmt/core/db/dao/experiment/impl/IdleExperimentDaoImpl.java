package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.IdleExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.TimestampedEntity_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.IdleExperiment;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository("idleExperimentDao")
public class IdleExperimentDaoImpl extends DaoImpl<IdleExperiment, Long> implements IdleExperimentDao {

    private static final Logger LOG = Logger.getLogger(IdleExperimentDaoImpl.class.getName());

    public IdleExperimentDaoImpl() {
        super(IdleExperiment.class);
    }

    @Override
    public void setIdleExperiment(final Experiment experiment) {
        final EntityManager entityManager = getEntityManager();
        final CriteriaBuilder cb = getCriteriaBuilder();

        final CriteriaDelete<IdleExperiment> query = cb.createCriteriaDelete(IdleExperiment.class);
        query.from(IdleExperiment.class);

        entityManager.createQuery(query).executeUpdate();

        if (experiment != null) {
            final IdleExperiment idleExperiment = new IdleExperiment();
            idleExperiment.setExperiment(experiment);
            persist(idleExperiment);
        }
    }

    @Override
    public Experiment getIdleExperiment() {
        final EntityManager entityManager = getEntityManager();
        final CriteriaBuilder cb = getCriteriaBuilder();

        final CriteriaQuery<IdleExperiment> query = cb.createQuery(IdleExperiment.class);
        final Root<IdleExperiment> idleExperimentRoot = query.from(IdleExperiment.class);

        query.orderBy(cb.desc(idleExperimentRoot.get(TimestampedEntity_.created)));

        final List<IdleExperiment> idleExperiments = getResultList(query);

        if (idleExperiments.isEmpty()) {
            return null;
        }

        if (idleExperiments.size() == 1) {
            return idleExperiments.get(0).getExperiment();
        }

        final IdleExperiment idleExperiment = idleExperiments.remove(0);
        final List<Long> ids = new ArrayList<>(idleExperiments.size());
        for (final IdleExperiment previousIdleExperiment : idleExperiments) {
            ids.add(previousIdleExperiment.getId());
            entityManager.detach(previousIdleExperiment);
        }
        deleteById(ids.toArray(new Long[ids.size()]));
        return idleExperiment.getExperiment();
    }

    protected int deleteById(final Long... ids) {
        return getInstantWriteTransactionTemplate().execute(transactionStatus -> {
            final EntityManager entityManager = getEntityManager();
            final CriteriaBuilder cb = getCriteriaBuilder();

            final CriteriaDelete<IdleExperiment> query = cb.createCriteriaDelete(IdleExperiment.class);
            final Root<IdleExperiment> idleExperimentRoot = query.from(IdleExperiment.class);

            query.where(idleExperimentRoot.get(GeneratedIdEntity_.id).in(ids));

            return entityManager.createQuery(query).executeUpdate();
        });
    }
}
