package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ExperimentType;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.Source;
import java.io.IOException;

/**
 Created by matthias on 16.03.15.
 */
public interface DesCriptReader {
    Experiment read(MultipartFile desCriptFile) throws IOException;

    Experiment read(Source source);

    Experiment read(ExperimentType experimentType);
}
