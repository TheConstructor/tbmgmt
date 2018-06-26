package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config;

import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.ExperimentMonitor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.NodeConfigMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by matthias on 13.11.15.
 */
@Configuration
@EnableScheduling
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Autowired
    private ExperimentMonitor experimentMonitor;

    @Autowired
    private NodeConfigMonitor nodeConfigMonitor;

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        // Every 10 seconds
        taskRegistrar.addFixedRateTask(experimentMonitor::readExperimentsFromDB, 10000);
        taskRegistrar.addFixedRateTask(nodeConfigMonitor::readNodeConfigFromDB, 10000);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(10);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService actionResultStoragePool(
            final ExperimentControlConfiguration experimentControlConfiguration) {
        return Executors.newFixedThreadPool(experimentControlConfiguration.getActionResultStoragePoolSize());
    }
}
