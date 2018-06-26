package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.logging.Logger;

/**
 Created by matthias on 06.04.15.
 */
@Service
public class InstantFormatter implements Formatter<Instant> {

    public static final ZoneId UTC = ZoneId.of("UTC");
    private static final Logger LOG = Logger.getLogger(InstantFormatter.class.getName());
    public static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .optionalStart()
            .optionalStart()
            .appendLiteral(' ')
            .optionalEnd()
            .appendOffset("+HH:mm", "+00:00")
            .toFormatter();

    @Override
    public Instant parse(final String text, final Locale locale) throws ParseException {
        if (text == null || "".equals(text)) {
            return null;
        }
        try {
            final TemporalAccessor temporalAccessor = DATE_TIME_FORMATTER.parse(text);
            // Zone-offset is optional. If present we can directly get INSTANT_SECONDS and thus Instant.from() will
            // work.
            if (temporalAccessor.isSupported(ChronoField.INSTANT_SECONDS)) {
                return Instant.from(temporalAccessor);
            } else {
                return LocalDateTime.from(temporalAccessor).atZone(ZoneId.systemDefault()).toInstant();
            }
        } catch (final DateTimeParseException e1) {
            throw new ParseException("Could not parse \"" + text + "\"", e1.getErrorIndex());
        } catch (final DateTimeException e1) {
            throw new ParseException("Could not convert \"" + text + "\"", 0);
        }
    }

    @Override
    public String print(final Instant object, final Locale locale) {
        if (object == null) {
            return "";
        }
        // We could make an option to select the time-zone for output - we would need to take care of failing tests then
        return DATE_TIME_FORMATTER.format(object.atZone(UTC));
    }
}
