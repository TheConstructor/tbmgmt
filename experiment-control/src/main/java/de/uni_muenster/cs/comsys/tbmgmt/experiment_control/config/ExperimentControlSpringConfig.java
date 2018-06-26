package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.CoreSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentMonitorImpl;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.NodeConfigMonitorImpl;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by matthias on 08.11.15.
 */
@Configuration
@Import({CoreSpringConfig.class, SchedulingConfiguration.class})
@EnableConfigurationProperties
public class ExperimentControlSpringConfig {

    @Bean
    public ExperimentMonitorImpl experimentMonitor() {
        return new ExperimentMonitorImpl();
    }

    @Bean
    public NodeConfigMonitorImpl nodeConfigMonitor() {
        return new NodeConfigMonitorImpl();
    }

    @Bean
    public ExperimentControlConfiguration experimentControlConfiguration() {
        return new ExperimentControlConfiguration();
    }

    @Bean
    public ActionResultStorageHelper actionResultStorageHelper() {
        return new ActionResultStorageHelper();
    }
}
