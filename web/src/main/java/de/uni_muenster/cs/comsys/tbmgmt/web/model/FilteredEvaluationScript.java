package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by matthias on 09.01.16.
 */
@Configurable
public class FilteredEvaluationScript implements Serializable {

    private final EvaluationScript evaluationScript;

    @Autowired
    private transient FileConfig fileConfig;

    public FilteredEvaluationScript(final EvaluationScript evaluationScript) {
        this.evaluationScript = evaluationScript;
    }

    public Long getId() {
        return evaluationScript.getId();
    }

    @NotBlank
    @Pattern(regexp = "[-._a-zA-Z0-9]+")
    @Length(min = 1, max = 42)
    public String getFileName() {
        return evaluationScript.getFileName();
    }

    public void setFileName(final String fileName) {
        evaluationScript.setFileName(fileName);
    }

    public MultipartFile getFile() {
        return null;
    }

    public void setFile(MultipartFile file) {
        if (file != null) {
            final File tempFile;
            try {
                tempFile = TbmgmtUtil.createTempFile(fileConfig.getUploadTempPath()).toFile();
                tempFile.deleteOnExit();
                file.transferTo(tempFile);
            } catch (IllegalStateException | IOException e) {
                throw new IllegalStateException("Could not move uploaded file to temporary directory");
            }
            evaluationScript.setFile(tempFile);
        }
    }

    public String getDescription() {
        return evaluationScript.getDescription();
    }

    public void setDescription(final String description) {
        evaluationScript.setDescription(description);
    }
}
