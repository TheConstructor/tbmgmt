package de.uni_muenster.cs.comsys.tbmgmt.web.model.experiment;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.variable.ExperimentVariableValue;

import java.io.Serializable;

/**
 * Created by matthias on 25.02.16.
 */
public class FilteredExperimentVariableValue implements Serializable {

    private final Experiment experiment;
    private final ExperimentVariableValue experimentVariableValue;

    public FilteredExperimentVariableValue(final Experiment experiment,
                                           final ExperimentVariableValue experimentVariableValue) {
        this.experiment = experiment;
        this.experimentVariableValue = experimentVariableValue;
    }

    public String getValue() {
        return experimentVariableValue.getValue();
    }

    public void setValue(final String value) {
        experimentVariableValue.setValue(value);
    }
}
