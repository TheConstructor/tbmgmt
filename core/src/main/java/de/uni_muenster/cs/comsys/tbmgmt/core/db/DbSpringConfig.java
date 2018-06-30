package de.uni_muenster.cs.comsys.tbmgmt.core.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptDatabaseHelper;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl.DesCriptDatabaseHelperImpl;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by matthias on 14.02.2015.
 */
@Configuration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@ComponentScan(basePackageClasses = DbSpringConfig.class)
@EnableConfigurationProperties
public class DbSpringConfig {

    @Bean
    public DbConfiguration dbConfiguration() {
        return new DbConfiguration();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DbConfiguration dbConfiguration,
                                                                       final ComboPooledDataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource);

        // Hibernate as persistence backend
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        final HashMap<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put(AvailableSettings.DIALECT, PostgreSQL95Dialect.class.getName());
        jpaProperties.put(AvailableSettings.HBM2DDL_AUTO, dbConfiguration.getHbm2ddlAuto());
        jpaProperties.put(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        jpaProperties.put(AvailableSettings.SHOW_SQL, dbConfiguration.getShowSql());
        jpaProperties.put(AvailableSettings.FORMAT_SQL, "true");
        jpaProperties.put(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, "true");
        jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                ImplicitNamingStrategyJpaCompliantImpl.class.getName());
        jpaProperties
                .put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, PhysicalNamingStrategyStandardImpl.class.getName());
        jpaProperties.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
        // next see https://stackoverflow.com/a/48601315/1266906
        jpaProperties.put(AvailableSettings.NON_CONTEXTUAL_LOB_CREATION, "true");
        // emmits warnings...
        // jpaProperties.put(AvailableSettings.RELEASE_CONNECTIONS, "AFTER_TRANSACTION");
        entityManagerFactoryBean.setJpaPropertyMap(jpaProperties);

        // No need for persistence.xml
        entityManagerFactoryBean.setPersistenceUnitName("tbmgmt-db");
        entityManagerFactoryBean.setPackagesToScan(DbSpringConfig.class.getPackage().getName());

        return entityManagerFactoryBean;
    }

    @Bean
    public ComboPooledDataSource dataSource(final DbConfiguration dbConfiguration)
            throws PropertyVetoException, SQLException {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(Driver.class.getName());
        dataSource.setJdbcUrl(dbConfiguration.getJdbcUrl());
        dataSource.setUser(dbConfiguration.getUser());
        dataSource.setPassword(dbConfiguration.getPassword());

        dataSource.setMinPoolSize(dbConfiguration.getMinPoolSize());
        dataSource.setInitialPoolSize(dbConfiguration.getMinPoolSize());
        dataSource.setMaxPoolSize(dbConfiguration.getMaxPoolSize());

        dataSource.setCheckoutTimeout(dbConfiguration.getCheckoutTimeout());
        dataSource.setLoginTimeout(dbConfiguration.getLoginTimeout());

        dataSource.setUnreturnedConnectionTimeout(dbConfiguration.getUnreturnedConnectionTimeout());
        dataSource.setDebugUnreturnedConnectionStackTraces(dbConfiguration.getUnreturnedConnectionTimeout() != 0);

        dataSource.setAcquireRetryAttempts(dbConfiguration.getAcquireRetryAttempts());
        dataSource.setAcquireRetryDelay(dbConfiguration.getAcquireRetryDelay());

        dataSource.setMaxIdleTimeExcessConnections(600);
        dataSource.setMaxIdleTime(3600);

        dataSource.setIdleConnectionTestPeriod(30);
        dataSource.setTestConnectionOnCheckin(false);
        dataSource.setTestConnectionOnCheckout(true);
        // should speed up things, when testConnectionOnCheckin is false
        dataSource.setForceSynchronousCheckins(true);

        // PreparedStatement Caching
        dataSource.setMaxStatementsPerConnection(dbConfiguration.getMaxStatementsPerConnection());
        dataSource.setStatementCacheNumDeferredCloseThreads(1);
        return dataSource;
    }

    @Bean
    @Autowired
    public JpaTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Autowired
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate;
    }

    @Bean
    @Autowired
    public TransactionTemplate instantWriteTransactionTemplate(final PlatformTransactionManager transactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }

    /**
     * This {@link TransactionTemplate} is mereley there to keep a {@link org.hibernate.Session} open and
     * {@link EntityManager#refresh(Object)} working
     */
    @Bean
    @Autowired
    public TransactionTemplate readOnlyTransactionTemplate(final PlatformTransactionManager transactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate;
    }

    @Bean
    public DesCriptDatabaseHelper desCriptDatabaseHelper() {
        return new DesCriptDatabaseHelperImpl();
    }
}
