package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostRemove;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 01.03.16.
 */
@MappedSuperclass
@Configurable
public abstract class FileEntity extends GeneratedIdEntity {
    private static final Logger LOG = Logger.getLogger(FileEntity.class.getName());

    @Autowired
    protected transient FileConfig fileConfig;

    protected String fileName;
    protected File file;
    protected String description;

    @Transient
    protected abstract Path getStoragePath();

    @Basic
    @Column(length = 10000)
    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Basic
    @Column(length = 10000)
    public String getFilePath() {
        return file == null ? null : getStoragePath().relativize(TbmgmtUtil.getRealPath(file)).toString();
    }

    public void setFilePath(final String filePath) {
        if (filePath == null) {
            file = null;
        } else {
            final Path basePath = getStoragePath();
            file = basePath.resolve(filePath).toFile();
        }
    }

    @Transient
    public File getFile() {
        return file;
    }

    @Transient
    public void setFile(final File file) {
        this.file = file;
    }

    @PrePersist
    @PreUpdate
    protected void persistFile() {
        if (file != null) {
            final Path basePath = getStoragePath();
            final Path uploadPath = fileConfig.getUploadTempPath();

            Path filePath = TbmgmtUtil.getRealPath(file);
            if (filePath.startsWith(uploadPath)) {
                try {
                    TbmgmtUtil.ensureDirectoryExists(basePath);
                } catch (final IOException e) {
                    throw new IllegalStateException("Storage path did not exist and could not be created.", e);
                }
                file = TbmgmtUtil.moveIntoBasePath(basePath, filePath).toFile();
                filePath = TbmgmtUtil.getRealPath(file);
            }

            if (!filePath.startsWith(basePath)) {
                throw new IllegalStateException(
                        "\"" + filePath + "\" refers to a file outside " + basePath + " and " + uploadPath);
            }
        }
    }

    @PostRemove
    protected void deleteFile() {
        if (file != null) {
            if (!file.delete()) {
                LOG.log(Level.WARNING, "The file " + file + " is no longer needed, but could not be removed");
            }
        }
    }

    @Basic
    @Column(length = 10000)
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("fileName", fileName)
                .append("file", file)
                .append("description", description)
                .toString();
    }

    protected void copyTo(final FileEntity to) {
        to.setFileName(getFileName());
        final File file = getFile();
        if (file != null && file.canRead()) {
            final Path uploadFilePath = fileConfig.getUploadTempPath();
            final Path tempFilePath = TbmgmtUtil.createTempFile(uploadFilePath);
            final File tempFile = tempFilePath.toFile();
            tempFile.deleteOnExit();
            try {
                Files.copy(file.toPath(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                to.setFile(tempFile);
            } catch (final IOException e) {
                LOG.log(Level.WARNING, "Could not copy " + file + " to " + tempFile, e);
            }
        }
        to.setDescription(getDescription());
    }
}
