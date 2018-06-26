package de.uni_muenster.cs.comsys.tbmgmt.core.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by matthias on 03.01.16.
 */
@ConfigurationProperties(prefix = "tbmgmt.files", ignoreUnknownFields = false)
public class FileConfig {

    private static final Log LOG = LogFactory.getLog(FileConfig.class.getName());

    private File uploadTempDir;
    private Path uploadTempPath;
    private File experimentFileStorageDir;
    private Path experimentFileStoragePath;
    private File evaluationScriptStorageDir;
    private Path evaluationScriptStoragePath;

    @PostConstruct
    protected void init() {
        LOG.debug("Using File-Configuration: " + this);
        uploadTempPath = resolvePath(uploadTempDir, "tbmgmt.files.uploadTmpDir");
        experimentFileStoragePath = resolvePath(experimentFileStorageDir, "tbmgmt.files.experimentFileStorageDir");
        evaluationScriptStoragePath =
                resolvePath(evaluationScriptStorageDir, "tbmgmt.files.evaluationScriptStorageDir");
    }

    private static Path resolvePath(final File uploadTempDir, final String propertyName) {
        try {
            final Path realPath = TbmgmtUtil.getRealPath(uploadTempDir.toPath());
            TbmgmtUtil.ensureDirectoryExists(realPath);
            return realPath;
        } catch (IllegalStateException | IOException | NullPointerException e) {
            throw new IllegalStateException("Could not resolve and verify " + propertyName, e);
        }
    }

    public File getUploadTempDir() {
        return uploadTempDir;
    }

    public void setUploadTempDir(final File uploadTempDir) {
        this.uploadTempDir = uploadTempDir;
    }

    public Path getUploadTempPath() {
        return uploadTempPath;
    }

    public File getExperimentFileStorageDir() {
        return experimentFileStorageDir;
    }

    public void setExperimentFileStorageDir(final File experimentFileStorageDir) {
        this.experimentFileStorageDir = experimentFileStorageDir;
    }

    public Path getExperimentFileStoragePath() {
        return experimentFileStoragePath;
    }

    public File getEvaluationScriptStorageDir() {
        return evaluationScriptStorageDir;
    }

    public void setEvaluationScriptStorageDir(final File evaluationScriptStorageDir) {
        this.evaluationScriptStorageDir = evaluationScriptStorageDir;
    }

    public Path getEvaluationScriptStoragePath() {
        return evaluationScriptStoragePath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uploadTempDir", uploadTempDir)
                .append("experimentFileStorageDir", experimentFileStorageDir)
                .append("evaluationScriptStorageDir", evaluationScriptStorageDir)
                .toString();
    }
}
