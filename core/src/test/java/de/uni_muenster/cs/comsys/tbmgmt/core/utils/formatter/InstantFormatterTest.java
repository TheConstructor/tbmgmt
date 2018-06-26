package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Created by matthias on 25.03.16.
 */
public class InstantFormatterTest extends AbstractFormatterTest {

    private static final Map<String, Instant> TEST_VALUES = ImmutableMap.<String, Instant>builder()
            .put("2016-02-24 23:59:59 +00:00",
                    OffsetDateTime.of(2016, 2, 24, 23, 59, 59, 0, ZoneOffset.ofHoursMinutes(0, 0)).toInstant())
            .put("2042-12-31 00:00:00 +00:00",
                    OffsetDateTime.of(2042, 12, 31, 0, 0, 0, 0, ZoneOffset.ofHoursMinutes(0, 0)).toInstant())
            .put("2042-12-30 23:00:00 +00:00",
                    OffsetDateTime.of(2042, 12, 31, 0, 0, 0, 0, ZoneOffset.ofHoursMinutes(1, 0)).toInstant())
            .build();
    private static final Map<String, Instant> PARSE_TEST_VALUES = ImmutableMap.<String, Instant>builder()
            .putAll(TEST_VALUES)
            .put("2016-02-24 23:59:59 -00:00",
                    OffsetDateTime.of(2016, 2, 24, 23, 59, 59, 0, ZoneOffset.ofHoursMinutes(0, 0)).toInstant())
            .put("2016-02-24 23:59:59+01:00",
                    OffsetDateTime.of(2016, 2, 24, 23, 59, 59, 0, ZoneOffset.ofHoursMinutes(1, 0)).toInstant())
            .put("2016-02-24 23:59:59",
                    ZonedDateTime.of(2016, 2, 24, 23, 59, 59, 0, ZoneId.systemDefault()).toInstant())
            .build();

    private InstantFormatter instantFormatter;

    @Before
    public void createInstance() {
        instantFormatter = new InstantFormatter();
    }

    @After
    public void cleanupInstance() {
        instantFormatter = null;
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testParse() throws Exception {
        AbstractFormatterTest.testParseValues(PARSE_TEST_VALUES, instantFormatter, null, null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testPrint() throws Exception {
        AbstractFormatterTest.testPrintValues(TEST_VALUES, instantFormatter, null, "");
    }
}