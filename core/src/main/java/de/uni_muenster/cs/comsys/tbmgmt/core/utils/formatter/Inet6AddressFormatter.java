package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 Created by matthias on 06.04.15.
 */
@Service
public class Inet6AddressFormatter implements Formatter<Inet6Address> {

    private static final Logger LOG = Logger.getLogger(Inet6AddressFormatter.class.getName());

    @Override
    public Inet6Address parse(final String text, final Locale locale) throws ParseException {
        if (text == null || "".equals(text)) {
            return null;
        }

        final InetAddress[] inetAddresses;
        try {
            inetAddresses = Inet6Address.getAllByName(text);
        } catch (final UnknownHostException e) {
            final ParseException parseException = new ParseException("Could not resolve address", 0);
            parseException.initCause(e);
            throw parseException;
        }

        for (final InetAddress inetAddress : inetAddresses) {
            if (inetAddress instanceof Inet6Address) {
                return (Inet6Address) inetAddress;
            }
        }

        throw new ParseException(String.format("Could not get an IPv6-address for \"%s\"", text), 0);
    }

    @Override
    public String print(final Inet6Address object, final Locale locale) {
        if (object == null) {
            return "";
        }
        return object.getHostAddress();
    }
}
