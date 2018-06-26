package de.uni_muenster.cs.comsys.tbmgmt.core.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.PostgresqlService;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.postgresql.Driver;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.beans.PropertyVetoException;
import java.util.HashMap;

/**
 * Created by matthias on 14.02.2015.
 */
@Configuration
@EnableSpringConfigured
@EnableAutoConfiguration
@Import(DbSpringConfig.class)
public class DbIntegrationTestSpringConfig {

    @Bean
    public PostgresqlService postgresqlService() {
        return new PostgresqlService();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final ComboPooledDataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean =
                new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(dataSource);

        // Hibernate as persistence backend
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        final HashMap<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put(AvailableSettings.DIALECT, PostgreSQL94Dialect.class.getName());
        jpaProperties.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
        jpaProperties.put(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        jpaProperties.put(AvailableSettings.SHOW_SQL, "true");
        jpaProperties.put(AvailableSettings.FORMAT_SQL, "true");
        jpaProperties.put(AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, "true");
        jpaProperties.put(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                ImplicitNamingStrategyJpaCompliantImpl.class.getName());
        jpaProperties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY,
                PhysicalNamingStrategyStandardImpl.class.getName());
        // jpaProperties.put(AvailableSettings.ENABLE_LAZY_LOAD_NO_TRANS, "true");
        entityManagerFactoryBean.setJpaPropertyMap(jpaProperties);

        // No need for persistence.xml
        entityManagerFactoryBean.setPersistenceUnitName("tbmgmt-db");
        entityManagerFactoryBean.setPackagesToScan(DbIntegrationTestSpringConfig.class.getPackage().getName());

        return entityManagerFactoryBean;
    }

    @Bean
    public ComboPooledDataSource dataSource(final PostgresqlService postgresqlService) throws PropertyVetoException {
        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(Driver.class.getName());
        dataSource.setJdbcUrl(postgresqlService.getJdbcUrl());
        dataSource.setUser(postgresqlService.getConfig().credentials().username());
        dataSource.setPassword(postgresqlService.getConfig().credentials().password());

        dataSource.setMinPoolSize(2);
        dataSource.setInitialPoolSize(2);
        dataSource.setMaxPoolSize(5);

        dataSource.setAcquireRetryAttempts(0);
        dataSource.setAcquireRetryDelay(1);

        dataSource.setMaxIdleTimeExcessConnections(600);
        dataSource.setMaxIdleTime(3600);

        dataSource.setIdleConnectionTestPeriod(30);
        dataSource.setTestConnectionOnCheckin(false);
        dataSource.setTestConnectionOnCheckout(true);

        // PreparedStatement Caching
        dataSource.setMaxStatementsPerConnection(200);
        return dataSource;
    }

    @Bean
    public InstantFormatter instantFormatter() {
        return new InstantFormatter();
    }

    @Bean
    public FileConfig fileConfig() {
        return new FileConfig();
    }
}
