package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.time.Duration;
import java.util.Map;

/**
 Created by matthias on 04.04.15.
 */
public class DurationFormatterTest extends AbstractFormatterTest {

    private static final Map<String, Duration> TEST_VALUES = ImmutableMap.<String, Duration>builder()
            .put("0", Duration.ZERO)
            .put("1s", Duration.ofSeconds(1))
            .put("59s", Duration.ofSeconds(59))
            .put("1m", Duration.ofMinutes(1))
            .put("59m", Duration.ofMinutes(59))
            .put("1m 1s", Duration.ofMinutes(1).plusSeconds(1))
            .put("59m 59s", Duration.ofMinutes(59).plusSeconds(59))
            .put("1h", Duration.ofHours(1))
            .put("23h", Duration.ofHours(23))
            .put("1h 1s", Duration.ofHours(1).plusSeconds(1))
            .put("23h 59s", Duration.ofHours(23).plusSeconds(59))
            .put("1h 1m", Duration.ofHours(1).plusMinutes(1))
            .put("23h 59m", Duration.ofHours(23).plusMinutes(59))
            .put("1h 1m 1s", Duration.ofHours(1).plusMinutes(1).plusSeconds(1))
            .put("23h 59m 59s", Duration.ofHours(23).plusMinutes(59).plusSeconds(59))
            .put("1d", Duration.ofDays(1))
            .put("42d", Duration.ofDays(42))
            .put("1d 1s", Duration.ofDays(1).plusSeconds(1))
            .put("53d 59s", Duration.ofDays(53).plusSeconds(59))
            .put("1d 1m", Duration.ofDays(1).plusMinutes(1))
            .put("64d 59m", Duration.ofDays(64).plusMinutes(59))
            .put("1d 1m 1s", Duration.ofDays(1).plusMinutes(1).plusSeconds(1))
            .put("31d 59m 59s", Duration.ofDays(31).plusMinutes(59).plusSeconds(59))
            .put("1d 1h", Duration.ofDays(1).plusHours(1))
            .put("20d 23h", Duration.ofDays(20).plusHours(23))
            .put("1d 1h 1s", Duration.ofDays(1).plusHours(1).plusSeconds(1))
            .put("9d 23h 59s", Duration.ofDays(9).plusHours(23).plusSeconds(59))
            .put("1d 1h 1m", Duration.ofDays(1).plusHours(1).plusMinutes(1))
            .put("75d 23h 59m", Duration.ofDays(75).plusHours(23).plusMinutes(59))
            .put("1d 1h 1m 1s", Duration.ofDays(1).plusHours(1).plusMinutes(1).plusSeconds(1))
            .put("100d 23h 59m 59s", Duration.ofDays(100).plusHours(23).plusMinutes(59).plusSeconds(59))
            .put("-1s", Duration.ofSeconds(1).negated())
            .put("-59s", Duration.ofSeconds(59).negated())
            .put("-1m", Duration.ofMinutes(1).negated())
            .put("-59m", Duration.ofMinutes(59).negated())
            .put("-1m 1s", Duration.ofMinutes(1).plusSeconds(1).negated())
            .put("-59m 59s", Duration.ofMinutes(59).plusSeconds(59).negated())
            .put("-1h", Duration.ofHours(1).negated())
            .put("-23h", Duration.ofHours(23).negated())
            .put("-1h 1s", Duration.ofHours(1).plusSeconds(1).negated())
            .put("-23h 59s", Duration.ofHours(23).plusSeconds(59).negated())
            .put("-1h 1m", Duration.ofHours(1).plusMinutes(1).negated())
            .put("-23h 59m", Duration.ofHours(23).plusMinutes(59).negated())
            .put("-1h 1m 1s", Duration.ofHours(1).plusMinutes(1).plusSeconds(1).negated())
            .put("-23h 59m 59s", Duration.ofHours(23).plusMinutes(59).plusSeconds(59).negated())
            .put("-1d", Duration.ofDays(1).negated())
            .put("-42d", Duration.ofDays(42).negated())
            .put("-1d 1s", Duration.ofDays(1).plusSeconds(1).negated())
            .put("-53d 59s", Duration.ofDays(53).plusSeconds(59).negated())
            .put("-1d 1m", Duration.ofDays(1).plusMinutes(1).negated())
            .put("-64d 59m", Duration.ofDays(64).plusMinutes(59).negated())
            .put("-1d 1m 1s", Duration.ofDays(1).plusMinutes(1).plusSeconds(1).negated())
            .put("-31d 59m 59s", Duration.ofDays(31).plusMinutes(59).plusSeconds(59).negated())
            .put("-1d 1h", Duration.ofDays(1).plusHours(1).negated())
            .put("-20d 23h", Duration.ofDays(20).plusHours(23).negated())
            .put("-1d 1h 1s", Duration.ofDays(1).plusHours(1).plusSeconds(1).negated())
            .put("-9d 23h 59s", Duration.ofDays(9).plusHours(23).plusSeconds(59).negated())
            .put("-1d 1h 1m", Duration.ofDays(1).plusHours(1).plusMinutes(1).negated())
            .put("-75d 23h 59m", Duration.ofDays(75).plusHours(23).plusMinutes(59).negated())
            .put("-1d 1h 1m 1s", Duration.ofDays(1).plusHours(1).plusMinutes(1).plusSeconds(1).negated())
            .put("-100d 23h 59m 59s", Duration.ofDays(100).plusHours(23).plusMinutes(59).plusSeconds(59).negated())
            .build();
    private static final Map<String, Duration> PARSE_TEST_VALUES = ImmutableMap.<String, Duration>builder()
            .putAll(TEST_VALUES)
            .put("", Duration.ZERO)
            .put("0s", Duration.ZERO)
            .put("0s ", Duration.ZERO)
            .put(" 0s", Duration.ZERO)
            .put("-0s", Duration.ZERO)
            .put("0m", Duration.ZERO)
            .put("-0m", Duration.ZERO)
            .put("0h", Duration.ZERO)
            .put("-0h", Duration.ZERO)
            .put("0d", Duration.ZERO)
            .put("-0d", Duration.ZERO)
            .put("100d 59s 23h 59m", Duration.ofDays(100).plusHours(23).plusMinutes(59).plusSeconds(59))
            .put("100d 59 23h 59m", Duration.ofDays(100).plusHours(23).plusMinutes(59).plusSeconds(59))
            .build();

    private DurationFormatter durationFormatter;

    @Before
    public void createInstance() {
        durationFormatter = new DurationFormatter();
    }

    @After
    public void cleanupInstance() {
        durationFormatter = null;
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testParse() throws Exception {
        AbstractFormatterTest.testParseValues(PARSE_TEST_VALUES, durationFormatter, null, null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testPrint() throws Exception {
        AbstractFormatterTest.testPrintValues(TEST_VALUES, durationFormatter, null, null);
    }

    @Test(expected = ParseException.class)
    public void testParseGarbage() throws Exception {
        durationFormatter.parse("/", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseLeadingGarbage() throws Exception {
        durationFormatter.parse("/0s", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseTrailingGarbage() throws Exception {
        durationFormatter.parse("0s/", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseUnknownUnit() throws Exception {
        durationFormatter.parse("0y", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseNumberExceedingLong() throws Exception {
        durationFormatter.parse("9223372036854775808", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseExceedingDuration() throws Exception {
        durationFormatter.parse("9223372036854775807m", null);
        Assert.fail("No ParseException");
    }

    @Test(expected = ParseException.class)
    public void testParseExceedingDuration2() throws Exception {
        durationFormatter.parse("9223372036854775807 1", null);
        Assert.fail("No ParseException");
    }
}