package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ExperimentType;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultsType;

import javax.xml.transform.Result;

/**
 Created by matthias on 29.03.15.
 */
public interface DesCriptWriter {
    void write(Experiment experiment, Result result);

    void write(Experiment experiment, ResultsType results, Result result);

    ExperimentType write(Experiment experiment);

    ExperimentType write(Experiment experiment, ResultsType results);
}
