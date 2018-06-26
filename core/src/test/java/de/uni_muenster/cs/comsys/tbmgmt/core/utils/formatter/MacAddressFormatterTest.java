package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import com.google.common.collect.ImmutableMap;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Created by matthias on 18.02.16.
 */
public class MacAddressFormatterTest extends AbstractFormatterTest {

    private static final Map<String, MacAddress> TEST_VALUES = ImmutableMap.<String, MacAddress>builder()
            .put("00-26-B9-9B-61-BF",
                    new MacAddress(new byte[]{0x00, 0x26, (byte) 0xB9, (byte) 0x9B, 0x61, (byte) 0xBF}))
            .put("42-12-36-DD-FD-CD",
                    new MacAddress(new byte[]{0x42, 0x12, 0x36, (byte) 0xdd, (byte) 0xfd, (byte) 0xcd}))
            .put("00-00-00-00-00-00", new MacAddress(new byte[0]))
            .build();

    private static final Map<String, MacAddress> PARSE_TEST_VALUES = ImmutableMap.<String, MacAddress>builder()
            .putAll(TEST_VALUES)
            .put("00:26:B9:9B:61:BF",
                    new MacAddress(new byte[]{0x00, 0x26, (byte) 0xB9, (byte) 0x9B, 0x61, (byte) 0xBF}))
            .put("42:12-36:DD-FD:CD",
                    new MacAddress(new byte[]{0x42, 0x12, 0x36, (byte) 0xdd, (byte) 0xfd, (byte) 0xcd}))
            .build();

    private MacAddressFormatter macAddressFormatter;

    @Before
    public void createInstance() {
        macAddressFormatter = new MacAddressFormatter();
    }

    @After
    public void cleanupInstance() {
        macAddressFormatter = null;
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testParse() throws Exception {
        AbstractFormatterTest.testParseValues(PARSE_TEST_VALUES, macAddressFormatter, null, null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testPrint() throws Exception {
        AbstractFormatterTest.testPrintValues(TEST_VALUES, macAddressFormatter, null, "");
    }
}