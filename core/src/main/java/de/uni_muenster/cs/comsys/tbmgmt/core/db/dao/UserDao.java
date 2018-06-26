package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by matthias on 15.02.2015.
 */
public interface UserDao extends GeneratedIdDao<User>, UserDetailsService {
    User loadByUsername(String username);
}
