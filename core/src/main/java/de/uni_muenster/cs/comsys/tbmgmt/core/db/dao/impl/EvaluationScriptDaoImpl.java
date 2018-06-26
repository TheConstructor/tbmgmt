package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.EvaluationScriptDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript_;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction_;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.StreamUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository("evaluationScriptDao")
public class EvaluationScriptDaoImpl extends DaoImpl<EvaluationScript, Long> implements EvaluationScriptDao {

    private static final Logger LOG = Logger.getLogger(EvaluationScriptDaoImpl.class.getName());

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private FileConfig fileConfig;

    public EvaluationScriptDaoImpl() {
        super(EvaluationScript.class);
    }

    @EventListener
    public void handleContextRefresh(@SuppressWarnings("UnusedParameters") final ContextRefreshedEvent event) {
        final Long count = entityCount();
        LOG.log(Level.INFO, "Init found {0} evaluation scripts", count);
        if (count == null || count <= 0) {
            final Path basePath = TbmgmtUtil.getRealPath(fileConfig.getEvaluationScriptStoragePath());
            try {
                for (final Resource resource : resourcePatternResolver.getResources(
                        ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "initial-scipts/*")) {
                    if (!resource.isReadable() || resource.getFilename() == null || StringUtils.endsWithIgnoreCase(
                            resource.getFilename(), ".bak") || StringUtils.endsWithAny(resource.getFilename(), "~")
                            || StringUtils.startsWithAny(resource.getFilename(), ".")) {
                        continue;
                    }
                    try {
                        try {
                            TbmgmtUtil.ensureDirectoryExists(basePath);
                        } catch (final IOException e) {
                            throw new IllegalStateException("Storage path did not exist and could not be created.", e);
                        }
                        final Path targetPath = TbmgmtUtil.createTempFile(basePath);
                        final File targetFile = targetPath.toFile();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                             InputStream inputStream = resource.getInputStream()) {
                            StreamUtils.copy(inputStream, fileOutputStream);
                        }
                        getTransactionTemplate().execute((TransactionStatus status) -> {
                            final EvaluationScript evaluationScript = new EvaluationScript();
                            evaluationScript.setFileName(resource.getFilename());
                            evaluationScript.setDescription("Imported from " + resource.getDescription());
                            evaluationScript.setFile(targetFile);

                            return merge(evaluationScript);
                        });
                    } catch (final IOException e) {
                        LOG.log(Level.WARNING,
                                "Could not import a evaluation script. If you need this you will need to import it "
                                        + "manually", e);
                    }
                }
            } catch (final IOException e) {
                LOG.log(Level.SEVERE,
                        "Could not import any evaluation script. If you need to import all scripts manually", e);
            }
        }
    }

    @Override
    public EvaluationScript getEvaluationScriptByFileName(final String fileName) {
        return getSingleResultByAttributeValue(EvaluationScript_.fileName, fileName);
    }

    @Override
    public EvaluationScript getEvaluationScriptById(final Long id) {
        return find(id);
    }

    @Override
    public List<EvaluationScript> getAllEvaluationScripts() {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<EvaluationScript> query = cb.createQuery(EvaluationScript.class);
        query.from(EvaluationScript.class);
        return getResultList(query);
    }

    @Override
    public Long countUsages(final EvaluationScript evaluationScript) {
        final CriteriaBuilder cb = getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<ExperimentAction> actionRoot = query.from(ExperimentAction.class);
        query.select(cb.count(actionRoot));
        query.where(cb.equal(actionRoot.get(ExperimentAction_.evaluationScript), evaluationScript));
        return getSingleResult(query);
    }
}
