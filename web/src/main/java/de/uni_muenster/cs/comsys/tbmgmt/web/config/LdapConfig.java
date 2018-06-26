package de.uni_muenster.cs.comsys.tbmgmt.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by matthias on 03.01.16.
 */
@ConfigurationProperties(prefix = "tbmgmt.web.ldap", ignoreUnknownFields = false)
public class LdapConfig {

    private String providerUrl;
    private String userDnPattern = "uid={0},ou=students,dc=testbed,dc=home";
    private String groupSearchBase = "ou=groups,dc=testbed,dc=home";

    private String adminGroupDn = "cn=testbed-admin-group,ou=groups,dc=testbed,dc=home";
    private String userGroupDn = "cn=testbed-user-group,ou=groups,dc=testbed,dc=home";

    // From "person"
    private String givenNameAttribute = "givenName";
    private String surNameAttribute = "sn";
    // From "inetOrgPerson"
    private String mailAttribute = "mail";
    /**
     * Refers to field with (usually) encrypted password. Stored to provide remember-me-feature.
     */
    private String passwordAttribute = "userPassword";

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(final String providerUrl) {
        this.providerUrl = providerUrl;
    }

    public String getUserDnPattern() {
        return userDnPattern;
    }

    public void setUserDnPattern(final String userDnPattern) {
        this.userDnPattern = userDnPattern;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(final String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getAdminGroupDn() {
        return adminGroupDn;
    }

    public void setAdminGroupDn(final String adminGroupDn) {
        this.adminGroupDn = adminGroupDn;
    }

    public String getUserGroupDn() {
        return userGroupDn;
    }

    public void setUserGroupDn(final String userGroupDn) {
        this.userGroupDn = userGroupDn;
    }

    public String getGivenNameAttribute() {
        return givenNameAttribute;
    }

    public void setGivenNameAttribute(final String givenNameAttribute) {
        this.givenNameAttribute = givenNameAttribute;
    }

    public String getSurNameAttribute() {
        return surNameAttribute;
    }

    public void setSurNameAttribute(final String surNameAttribute) {
        this.surNameAttribute = surNameAttribute;
    }

    public String getMailAttribute() {
        return mailAttribute;
    }

    public void setMailAttribute(final String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(final String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }
}
