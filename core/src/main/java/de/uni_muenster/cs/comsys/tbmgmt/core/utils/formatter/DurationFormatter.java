package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Created by matthias on 04.04.15.
 */
@Service
public class DurationFormatter implements Formatter<Duration> {

    public static final  int     SECONDS_PER_MINUTE = 60;
    public static final  int     MINUTES_PER_HOUR   = 60;
    public static final  int     HOURS_PER_DAY      = 24;
    public static final Pattern SEGMENT_PATTERN = Pattern.compile("\\s*([0-9]+)\\s*([a-zA-Z]*)\\s*");
    public static final  Pattern SIGN_PATTERN       = Pattern.compile("^\\s*([-+])");
    private static final Logger  LOG                = Logger.getLogger(DurationFormatter.class.getName());

    public static String print(final Duration object) {
        if (object == null) {
            return null;
        }

        final long totalSeconds = object.abs().getSeconds();

        if (totalSeconds == 0) {
            return "0";
        }

        final long seconds = totalSeconds % SECONDS_PER_MINUTE;
        final long totalMinutes = totalSeconds / SECONDS_PER_MINUTE;
        final long minutes = totalMinutes % MINUTES_PER_HOUR;
        final long totalHours = totalMinutes / MINUTES_PER_HOUR;
        final long hours = totalHours % HOURS_PER_DAY;
        final long totalDays = totalHours / HOURS_PER_DAY;

        final StringBuilder stringBuilder = new StringBuilder();
        if (object.isNegative()) {
            stringBuilder.append("-");
        }
        if (totalDays != 0) {
            stringBuilder.append(totalDays).append("d ");
        }
        if (hours != 0) {
            stringBuilder.append(hours).append("h ");
        }
        if (minutes != 0) {
            stringBuilder.append(minutes).append("m ");
        }
        if (seconds != 0) {
            stringBuilder.append(seconds).append("s ");
        }

        // remove trailing space
        stringBuilder.setLength(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    @Override
    public Duration parse(final String text, final Locale locale) throws ParseException {
        if (text == null) {
            return null;
        }

        if (StringUtils.isBlank(text)) {
            return Duration.ZERO;
        }

        Duration duration = Duration.ZERO;
        final boolean negative;
        int pos = 0;

        final Matcher signMatcher = SIGN_PATTERN.matcher(text);
        if (signMatcher.find()) {
            negative = "-".equals(signMatcher.group(1));
            pos = signMatcher.end();
        } else {
            negative = false;
        }

        // New Matcher and find with pos. First to ensure SEGMENT_PATTERN is compiled, second to ensure matches of
        // SIGN_PATTERN are correctly skipped; this would be necessary even with the same Matcher if text starts with
        // whitespace.
        final Matcher matcher = SEGMENT_PATTERN.matcher(text);
        if (matcher.find(pos)) {
            do {
                if (matcher.start() != pos) {
                    throw createParseException(text, pos, matcher.start());
                }
                pos = matcher.end();
                final long value;
                try {
                    value = Long.parseLong(matcher.group(1));
                } catch (final NumberFormatException e) {
                    LOG.log(Level.FINE, "Could not parse number", e);
                    throw createParseException(text, matcher.start(1), matcher.end(1));
                }
                try {
                    if (StringUtils.isNotBlank(matcher.group(2))) {
                        switch (matcher.group(2)) {
                            case "d":
                                duration = duration.plusDays(value);
                                break;
                            case "h":
                                duration = duration.plusHours(value);
                                break;
                            case "m":
                                duration = duration.plusMinutes(value);
                                break;
                            case "s":
                                duration = duration.plusSeconds(value);
                                break;
                            default:
                                throw createParseException(text, matcher.start(2), matcher.end(2));
                        }
                    } else {
                        duration = duration.plusSeconds(value);
                    }
                } catch (final ArithmeticException e) {
                    throw createParseException(text, matcher.start(1), matcher.end(2));
                }
            } while (matcher.find());
        }
        if (pos != text.length()) {
            throw createParseException(text, pos, text.length());
        }

        if (negative) {
            return duration.negated();
        }
        return duration;
    }

    private static ParseException createParseException(final String text, final int firstUnknown,
                                                       final int lastUnknown) {
        return new ParseException(
                "Can not interpret \"" + text.substring(firstUnknown, lastUnknown) + "\" in \"" + text + "\"",
                firstUnknown);
    }

    @Override
    public String print(final Duration object, final Locale locale) {
        return print(object);
    }
}
