package de.uni_muenster.cs.comsys.tbmgmt.core.db.entities;

import com.google.common.collect.Lists;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.userTypes.JsonbUserType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Collection;

/**
 * A User.
 */
@Entity
public class User extends GeneratedIdEntity implements UserDetails {
    private static final SimpleAuthorityMapper AUTHORITY_MAPPER = new SimpleAuthorityMapper();

    private String userName;
    private String givenName;
    private String surName;
    private String eMail;
    private String password;
    private String[] roles;

    @Basic
    @Column(unique = true)
    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @Basic
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    @Basic
    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    @Basic
    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    @Basic
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(columnDefinition = JsonbUserType.PG_TYPE_STRING)
    @Type(type = JsonbUserType.TYPE_STRING)
    public String[] getRoles() {
        return roles;
    }

    public void setRoles(final String[] roles) {
        this.roles = roles;
    }

    @Override
    @Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AUTHORITY_MAPPER
                .mapAuthorities(Lists.transform(Arrays.asList(getRoles()), SimpleGrantedAuthority::new));
    }

    @Override
    @Transient
    public String getUsername() {
        return getUserName();
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                                        .append("givenName", givenName)
                                        .append("surName", surName)
                                        .append("eMail", eMail)
                                        .append("password", password)
                                        .append("roles", roles)
                                        .toString();
    }
}
