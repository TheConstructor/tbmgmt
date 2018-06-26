package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.Tag;

import java.util.List;

/**
 * Created by matthias on 16.03.15.
 */
public interface TagDao extends GeneratedIdDao<Tag> {
    Tag getByName(String name);

    List<String> getNames();

    Long countUsages(Tag tag);
}
