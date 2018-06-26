package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.DurationFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet4AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet6AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.MacAddressFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistrar;

/**
 Created by matthias on 21.05.15.
 */
@Configuration
public class UtilsSpringConfig {

    @Bean
    public DurationFormatter durationFormatter() {
        return new DurationFormatter();
    }

    @Bean
    public InstantFormatter instantFormatter() {
        return new InstantFormatter();
    }

    @Bean
    public MacAddressFormatter macAddressFormatter() {
        return new MacAddressFormatter();
    }

    @Bean
    public Inet4AddressFormatter inet4AddressFormatter() {
        return new Inet4AddressFormatter();
    }

    @Bean
    public Inet6AddressFormatter inet6AddressFormatter() {
        return new Inet6AddressFormatter();
    }

    @Bean
    public FormatterRegistrar coreFormatterRegistrar() {
        return new CoreFormatterRegistrar();
    }
}
