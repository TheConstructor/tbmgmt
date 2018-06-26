package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.TagDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment_;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository("tagDao")
public class TagDaoImpl extends DaoImpl<Tag, Long> implements TagDao {

    private static final Logger LOG = Logger.getLogger(TagDaoImpl.class.getName());

    public TagDaoImpl() {
        super(Tag.class);
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("UnusedParameters") final ContextRefreshedEvent event) {
        final Long count = entityCount();
        LOG.log(Level.INFO, "Init found {0} tags", count);
        if (count == null || count <= 0) {
            for (final String name : new String[]{"playground", "thesis"}) {
                getTransactionTemplate().execute((TransactionStatus status) -> {
                    final Tag tag = new Tag();
                    tag.setName(name);

                    return merge(tag);
                });
            }
        }
    }

    @Override
    public Tag getByName(final String name) {
        return getSingleResultByAttributeValue(Tag_.name, name);
    }

    @Override
    public List<String> getNames() {
        return getAllAttributeValues(Tag_.name, String.class);
    }

    @Override
    public Long countUsages(final Tag tag) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Experiment> experimentRoot = query.from(Experiment.class);
        query.select(cb.count(experimentRoot));
        query.where(cb.isMember(tag, experimentRoot.get(Experiment_.tags)));
        return getSingleResult(query);
    }
}
