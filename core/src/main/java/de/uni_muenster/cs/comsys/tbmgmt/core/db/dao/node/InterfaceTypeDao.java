package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.GeneratedIdDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;

import java.util.List;

/**
 * Created by matthias on 16.03.15.
 */
public interface InterfaceTypeDao extends GeneratedIdDao<InterfaceType> {
    InterfaceType getByName(String name);

    List<String> getNames();

    Long countUsages(InterfaceType interfaceType);
}
