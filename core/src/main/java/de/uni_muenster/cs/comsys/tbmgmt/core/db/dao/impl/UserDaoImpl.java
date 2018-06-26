package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.UserDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User_;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * DAO and {@link UserDetailsService} to interact with stored {@link User}-Entities.
 */
@Repository("userDao")
public class UserDaoImpl extends DaoImpl<User, Long> implements UserDao {

    private static final Logger                LOG              = Logger.getLogger(UserDaoImpl.class.getName());
    private static final SimpleAuthorityMapper AUTHORITY_MAPPER = new SimpleAuthorityMapper();

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user;
        try {
            user = loadByUsername(username);
        } catch (NoSuchElementException | EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("Could not load user by username", e);
        }
        getEntityManager().detach(user);
        return user;
    }

    @Override
    public User loadByUsername(String username) {
        return getSingleResultByAttributeValue(User_.userName, username);
    }

}
