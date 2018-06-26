package de.uni_muenster.cs.comsys.tbmgmt.core.db;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.QueryHints;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

/**
 * Most settings here directly set values described in
 * <a href="http://www.mchange.com/projects/c3p0/#configuration_properties">c3p0-documentation</a>
 */
@ConfigurationProperties(prefix = "tbmgmt.db", ignoreUnknownFields = false)
public class DbConfiguration {

    private static final Log LOG = LogFactory.getLog(DbConfiguration.class.getName());

    /**
     * Setting to perform SchemaManagementTool actions automatically as part of
     * the SessionFactory lifecycle.  Valid options are defined by the
     * {@link org.hibernate.tool.schema.Action} enum.
     *
     * @see org.hibernate.tool.schema.Action
     * @see AvailableSettings#HBM2DDL_AUTO
     */
    private String hbm2ddlAuto = "validate";
    private int    maxPoolSize = 20;
    private int    minPoolSize = 5;
    private String password    = "";
    private String user        = "tbmgmt";
    private String jdbcUrl;
    /**
     * Enable logging of generated SQL to the console
     *
     * @see AvailableSettings#SHOW_SQL
     */
    private String showSql = "false";
    /**
     * The number of milliseconds a client calling getConnection() will wait for a Connection to be checked-in or
     * acquired when the pool is exhausted. Zero means wait indefinitely. Setting any positive value will cause the
     * getConnection() call to time-out and break with an SQLException after the specified number of milliseconds.
     */
    private int checkoutTimeout = 60000;
    private int acquireRetryAttempts = 10;
    private int acquireRetryDelay = 1;
    private int loginTimeout = 10000;
    /**
     * unreturnedConnectionTimeout defines a limit (in seconds) to how long a Connection may remain checked out. If
     * set to a nonzero value, unreturned, checked-out Connections that exceed this limit will be summarily destroyed,
     * and then replaced in the pool.
     * <p><strong>Don't set to != 0 for experiment-control!</strong></p>
     */
    private int unreturnedConnectionTimeout = 0;
    /**
     * You need to set a value > 0 to enable PreparedStatements caching
     */
    private int maxStatementsPerConnection = 42;
    /**
     * Query-Timeout in milliseconds, set via {@link QueryHints#SPEC_HINT_TIMEOUT}. Hibernate only uses full seconds
     * of this value ({@code queryTimeout / 1000})
     */
    private int queryTimeout = 180000;

    @PostConstruct
    public void init() {
        LOG.debug("Using DB-Configuration: " + this);
    }

    public String getShowSql() {
        return showSql;
    }

    public void setShowSql(final String showSql) {
        this.showSql = showSql;
    }

    public String getHbm2ddlAuto() {
        return hbm2ddlAuto;
    }

    public void setHbm2ddlAuto(final String hbm2ddlAuto) {
        this.hbm2ddlAuto = hbm2ddlAuto;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public int getCheckoutTimeout() {
        return checkoutTimeout;
    }

    public void setCheckoutTimeout(final int checkoutTimeout) {
        this.checkoutTimeout = checkoutTimeout;
    }

    public int getAcquireRetryAttempts() {
        return acquireRetryAttempts;
    }

    public void setAcquireRetryAttempts(final int acquireRetryAttempts) {
        this.acquireRetryAttempts = acquireRetryAttempts;
    }

    public int getAcquireRetryDelay() {
        return acquireRetryDelay;
    }

    public void setAcquireRetryDelay(final int acquireRetryDelay) {
        this.acquireRetryDelay = acquireRetryDelay;
    }

    public int getLoginTimeout() {
        return loginTimeout;
    }

    public void setLoginTimeout(final int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }

    public int getUnreturnedConnectionTimeout() {
        return unreturnedConnectionTimeout;
    }

    public void setUnreturnedConnectionTimeout(final int unreturnedConnectionTimeout) {
        this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
    }

    public int getMaxStatementsPerConnection() {
        return maxStatementsPerConnection;
    }

    public void setMaxStatementsPerConnection(final int maxStatementsPerConnection) {
        this.maxStatementsPerConnection = maxStatementsPerConnection;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(final int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("hbm2ddlAuto", hbm2ddlAuto)
                .append("maxPoolSize", maxPoolSize)
                .append("minPoolSize", minPoolSize)
                .append("password", password)
                .append("user", user)
                .append("jdbcUrl", jdbcUrl)
                .append("showSql", showSql)
                .append("checkoutTimeout", checkoutTimeout)
                .append("acquireRetryAttempts", acquireRetryAttempts)
                .append("acquireRetryDelay", acquireRetryDelay)
                .append("loginTimeout", loginTimeout)
                .append("unreturnedConnectionTimeout", unreturnedConnectionTimeout)
                .append("maxStatementsPerConnection", maxStatementsPerConnection)
                .append("queryTimeout", queryTimeout)
                .toString();
    }
}
