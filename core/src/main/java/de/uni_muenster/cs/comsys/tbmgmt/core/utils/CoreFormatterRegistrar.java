package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.DurationFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet4AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.Inet6AddressFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.MacAddressFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.Duration;
import java.time.Instant;

/**
 Created by matthias on 04.04.15.
 */
@Service
public class CoreFormatterRegistrar implements FormatterRegistrar {

    @Autowired
    private DurationFormatter durationFormatter;

    @Autowired
    private InstantFormatter instantFormatter;

    @Autowired
    private MacAddressFormatter macAddressFormatter;

    @Autowired
    private Inet4AddressFormatter inet4AddressFormatter;

    @Autowired
    private Inet6AddressFormatter inet6AddressFormatter;

    @Override
    public void registerFormatters(final FormatterRegistry registry) {
        registry.addFormatterForFieldType(Duration.class, durationFormatter);
        registry.addFormatterForFieldType(Instant.class, instantFormatter);
        registry.addFormatterForFieldType(MacAddress.class, macAddressFormatter);
        registry.addFormatterForFieldType(Inet4Address.class, inet4AddressFormatter);
        registry.addFormatterForFieldType(Inet6Address.class, inet6AddressFormatter);
    }
}
