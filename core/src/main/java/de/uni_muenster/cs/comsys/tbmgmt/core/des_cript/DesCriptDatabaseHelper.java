package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.schema.ResultsType;

/**
 * Created by matthias on 05.03.16.
 */
public interface DesCriptDatabaseHelper {
    ResultsType loadResultsFromDatabase(Experiment experiment);
}
