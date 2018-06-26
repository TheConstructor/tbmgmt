package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 Created by matthias on 06.04.15.
 */
@Service
public class MacAddressFormatter implements Formatter<MacAddress> {

    private static final Logger LOG = Logger.getLogger(MacAddressFormatter.class.getName());

    @Override
    public MacAddress parse(final String text, final Locale locale) throws ParseException {
        if (text == null || "".equals(text)) {
            return null;
        }

        return new MacAddress(text);
    }

    @Override
    public String print(final MacAddress object, final Locale locale) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }
}
