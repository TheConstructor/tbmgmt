package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.result.ExperimentLogEntryDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentLogEntry;
import org.springframework.stereotype.Repository;

import java.util.logging.Logger;

@Repository("experimentLogEntryDao")
public class ExperimentLogEntryDaoImpl extends DaoImpl<ExperimentLogEntry, Long> implements ExperimentLogEntryDao {

    private static final Logger LOG = Logger.getLogger(ExperimentLogEntryDaoImpl.class.getName());

    public ExperimentLogEntryDaoImpl() {
        super(ExperimentLogEntry.class);
    }
}
