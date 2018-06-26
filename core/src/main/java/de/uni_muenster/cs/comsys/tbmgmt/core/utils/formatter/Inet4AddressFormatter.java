package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 Created by matthias on 06.04.15.
 */
@Service
public class Inet4AddressFormatter implements Formatter<Inet4Address> {

    private static final Logger LOG = Logger.getLogger(Inet4AddressFormatter.class.getName());

    @Override
    public Inet4Address parse(final String text, final Locale locale) throws ParseException {
        if (text == null || "".equals(text)) {
            return null;
        }

        final InetAddress[] inetAddresses;
        try {
            inetAddresses = Inet4Address.getAllByName(text);
        } catch (final UnknownHostException e) {
            final ParseException parseException = new ParseException("Could not resolve address", 0);
            parseException.initCause(e);
            throw parseException;
        }

        for (final InetAddress inetAddress : inetAddresses) {
            if (inetAddress instanceof Inet4Address) {
                return (Inet4Address) inetAddress;
            }
        }

        throw new ParseException(String.format("Could not get an IPv4-address for \"%s\"", text), 0);
    }

    @Override
    public String print(final Inet4Address object, final Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getHostAddress();
    }
}
