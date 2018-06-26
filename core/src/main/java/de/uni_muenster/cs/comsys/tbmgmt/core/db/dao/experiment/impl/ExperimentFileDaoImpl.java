package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentFileDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl.DaoImpl;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentFile;
import org.springframework.stereotype.Repository;

import java.util.logging.Logger;

@Repository("experimentFileDao")
public class ExperimentFileDaoImpl extends DaoImpl<ExperimentFile, Long> implements ExperimentFileDao {

    private static final Logger LOG = Logger.getLogger(ExperimentFileDaoImpl.class.getName());

    public ExperimentFileDaoImpl() {
        super(ExperimentFile.class);
    }
}
