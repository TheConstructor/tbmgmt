package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.Validateable;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by matthias on 09.01.16.
 */
@Configurable
public class FilteredExperimentFile implements Serializable, Validateable {

    private final Experiment experiment;
    private final ExperimentFile experimentFile;

    @Autowired
    private transient FileConfig fileConfig;

    public FilteredExperimentFile(final Experiment experiment, final ExperimentFile experimentFile) {
        this.experiment = experiment;
        this.experimentFile = experimentFile;
    }

    public Long getId() {
        return experimentFile.getId();
    }

    @NotBlank
    @Pattern(regexp = "[-._a-zA-Z0-9]+")
    @Length(min = 1, max = 42)
    public String getFileName() {
        return experimentFile.getFileName();
    }

    public void setFileName(final String fileName) {
        experimentFile.setFileName(fileName);
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
            experimentFile.setFile(tempFile);
        }
    }

    public String getDescription() {
        return experimentFile.getDescription();
    }

    public void setDescription(final String description) {
        experimentFile.setDescription(description);
    }

    public boolean isEval() {
        return experimentFile.isEval();
    }

    public void setEval(final boolean eval) {
        experimentFile.setEval(eval);
    }

    @Override
    public void validate(final ValidationContext validationContext) {
        final MessageContext messageContext = validationContext.getMessageContext();
        for (final ExperimentFile file : experiment.getFiles()) {
            //noinspection ObjectEquality
            if (Objects.equals(file.getFileName(), experimentFile.getFileName()) && file != experimentFile) {
                messageContext.addMessage(new MessageBuilder()
                        .error().source("fileName")
                        .defaultText("File-names need to be unique")
                        .build());
                break;
            }
        }

        final Optional<String> evaluationScriptOfSameName = experiment
                .getActionBlocks()
                .stream()
                .flatMap(ab -> ab.getActions().stream().filter(a -> a.getEvaluationScript() != null))
                .map(a -> a.getEvaluationScript().getFileName())
                .filter(fn -> Objects.equals(fn, experimentFile.getFileName()))
                .findAny();

        if (evaluationScriptOfSameName.isPresent()) {
            messageContext.addMessage(new MessageBuilder()
                    .error().source("fileName")
                    .defaultText("File-name is also used by an evaluation-script of your experiment")
                    .build());
        }
    }
}
