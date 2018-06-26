package de.uni_muenster.cs.comsys.tbmgmt.web.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.UserDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.TreeSet;

/**
 * Created by matthias on 12.03.16.
 */
public class DbBackedLdapUserDetailsMapper extends LdapUserDetailsMapper {
    @Autowired
    private UserDao userDao;

    @Autowired
    private LdapConfig ldapConfig;

    public DbBackedLdapUserDetailsMapper() {
    }

    @Override
    public UserDetails mapUserFromContext(final DirContextOperations ctx, final String username,
                                          final Collection<? extends GrantedAuthority> authorities) {
        final UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        return userDao.getTransactionTemplate().execute(status -> {
            final User dbUser = loadDbUser(username);
            // From "person"
            dbUser.setGivenName(ctx.getStringAttribute(ldapConfig.getGivenNameAttribute()));
            dbUser.setSurName(ctx.getStringAttribute(ldapConfig.getSurNameAttribute()));
            // From "inetOrgPerson"
            dbUser.setEMail(ctx.getStringAttribute(ldapConfig.getMailAttribute()));
            final Object passo = ctx.getObjectAttribute(ldapConfig.getPasswordAttribute());
            if (passo != null) {
                final String password = LdapUtils.convertPasswordToString(passo);
                dbUser.setPassword(password);
            }

            final TreeSet<String> roles = new TreeSet<>();
            for (final GrantedAuthority authority : userDetails.getAuthorities()) {
                if (authority == null) {
                    continue;
                }
                if (authority instanceof LdapAuthority) {
                    final LdapAuthority ldapAuthority = (LdapAuthority) authority;
                    if (ldapConfig.getAdminGroupDn().equals(ldapAuthority.getDn())) {
                        roles.add("ADMIN");
                        roles.add("USER");
                    } else if (ldapConfig.getUserGroupDn().equals(ldapAuthority.getDn())) {
                        roles.add("USER");
                    }
                    roles.add(ldapAuthority.getDn());
                } else if (StringUtils.startsWith(authority.getAuthority(), "ROLE_")) {
                    roles.add(StringUtils.removeStart(authority.getAuthority(), "ROLE_"));
                }
            }
            dbUser.setRoles(roles.toArray(new String[roles.size()]));
            return dbUser;
        });
    }

    private User loadDbUser(final String username) {
        try {
            return userDao.loadByUsername(username);
        } catch (NoSuchElementException | EmptyResultDataAccessException e) {
            final User user = new User();
            user.setUserName(username);
            userDao.persist(user);
            return user;
        }
    }
}
