package de.uni_muenster.cs.comsys.tbmgmt.core;

import de.uni_muenster.cs.comsys.tbmgmt.core.config.FileConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.UtilsSpringConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

/**
 * Created by matthias on 10.03.15.
 */
@Configuration
@EnableSpringConfigured
@Import({UtilsSpringConfig.class, DbSpringConfig.class, DesCriptSpringConfig.class})
public class CoreSpringConfig {

    @Bean
    public FileConfig fileConfig() {
        return new FileConfig();
    }
}
