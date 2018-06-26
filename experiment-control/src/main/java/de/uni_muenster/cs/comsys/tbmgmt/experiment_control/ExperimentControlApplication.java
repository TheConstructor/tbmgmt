package de.uni_muenster.cs.comsys.tbmgmt.experiment_control;

import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlSpringConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Configuration
@Import({ExperimentControlSpringConfig.class})
public class ExperimentControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExperimentControlApplication.class, args);
    }
}
