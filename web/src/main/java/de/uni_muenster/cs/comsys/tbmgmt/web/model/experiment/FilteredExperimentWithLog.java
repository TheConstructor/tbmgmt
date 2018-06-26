package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentLogEntryDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentReplicationResultDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.GeneratedIdEntity_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogLevel;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.model.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Model for viewExperiment. Remember to add binding in viewExperiment-flow.xml if you want to be able to set
 * something in the view.
 */
@Configurable
public class FilteredExperimentWithLog extends FilteredExperiment implements Serializable {

    @Autowired
    private transient ExperimentReplicationResultDao replicationResultDao;

    @Autowired
    private transient ExperimentLogEntryDao logEntryDao;

    private String onlyContaining = null;
    private Long onlyReplication = null;
    private Long onlyIteration = null;
    private Long onlyNode = null;
    private Long onlyActionBlock = null;
    private Long onlyAction = null;
    private LogLevel onlyLogLevel = null;

    private final Experiment experiment;

    public FilteredExperimentWithLog(final Experiment experiment) {
        super(experiment);
        this.experiment = experiment;
    }

    public String getOnlyContaining() {
        return onlyContaining;
    }

    public void setOnlyContaining(final String onlyContaining) {
        this.onlyContaining = onlyContaining;
    }

    public Long getOnlyReplication() {
        return onlyReplication;
    }

    public void setOnlyReplication(final Long onlyReplication) {
        this.onlyReplication = onlyReplication;
    }

    public Long getOnlyIteration() {
        return onlyIteration;
    }

    public void setOnlyIteration(final Long onlyIteration) {
        this.onlyIteration = onlyIteration;
    }

    public Long getOnlyNode() {
        return onlyNode;
    }

    public void setOnlyNode(final Long onlyNode) {
        this.onlyNode = onlyNode;
    }

    public Long getOnlyActionBlock() {
        return onlyActionBlock;
    }

    public void setOnlyActionBlock(final Long onlyActionBlock) {
        this.onlyActionBlock = onlyActionBlock;
    }

    public Long getOnlyAction() {
        return onlyAction;
    }

    public void setOnlyAction(final Long onlyAction) {
        this.onlyAction = onlyAction;
    }

    public LogLevel getOnlyLogLevel() {
        return onlyLogLevel;
    }

    public void setOnlyLogLevel(final LogLevel onlyLogLevel) {
        this.onlyLogLevel = onlyLogLevel;
    }

    public List<Node> getUsedNodes() {
        final ArrayList<Node> nodes = new ArrayList<>(experiment.getUsedNodes().values());
        nodes.sort((n1, n2) -> n1.getName().compareTo(n2.getName()));
        return nodes;
    }

    public Pagination<ExperimentLogEntry> getLogEntries(final String[] pageParam, final String[] perPageParam) {
        int page = 0;
        int perPage = 20;
        if (pageParam != null && pageParam.length > 0) {
            try {
                page = Integer.parseInt(pageParam[0]);
            } catch (NumberFormatException e) {
            }
        }
        if (perPageParam != null && perPageParam.length > 0) {
            try {
                perPage = Integer.parseInt(perPageParam[0]);
            } catch (NumberFormatException e) {
            }
        }
        return new Pagination<>(logEntryDao, (cb, logEntryRoot) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(logEntryRoot.get(ExperimentLogEntry_.experiment), experiment));
            if (StringUtils.isNotBlank(onlyContaining)) {
                predicates.add(cb.like(cb.lower(logEntryRoot.get(ExperimentLogEntry_.message)),
                        cb.lower(cb.literal("%" + TbmgmtUtil.escapeLikeString(onlyContaining) + "%")),
                        TbmgmtUtil.LIKE_ESCAPE_CHAR));
            }
            addIdEqualsPredicate(cb, predicates, () -> logEntryRoot.get(ExperimentLogEntry_.replicationResult),
                    onlyReplication);
            addIdEqualsPredicate(cb, predicates, () -> logEntryRoot.get(ExperimentLogEntry_.variableValues),
                    onlyIteration);
            addIdEqualsPredicate(cb, predicates, () -> logEntryRoot.get(ExperimentLogEntry_.node), onlyNode);
            if (onlyAction != null || onlyActionBlock != null) {
                final Path<ExperimentAction> actionPath = logEntryRoot.get(ExperimentLogEntry_.action);
                addIdEqualsPredicate(cb, predicates, () -> actionPath.get(ExperimentAction_.experimentActionBlock),
                        onlyActionBlock);
                addIdEqualsPredicate(cb, predicates, () -> actionPath, onlyAction);
            }
            if (onlyLogLevel != null) {
                predicates.add(cb.equal(logEntryRoot.get(ExperimentLogEntry_.logLevel), onlyLogLevel));
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        }, (cb, logEntryRoot) -> {
            return Collections.singletonList(cb.desc(logEntryRoot.get(ExperimentLogEntry_.created)));
        }, perPage, page, "");
    }

    // path as Supplier so we don't generate unnecessary joins
    private static <T extends GeneratedIdEntity> void addIdEqualsPredicate(final CriteriaBuilder cb,
                                                                           final List<Predicate> predicates,
                                                                           final Supplier<Path<T>> path,
                                                                           final Long value) {
        if (value != null) {
            predicates.add(cb.equal(path.get().get(GeneratedIdEntity_.id), value));
        }
    }
}
