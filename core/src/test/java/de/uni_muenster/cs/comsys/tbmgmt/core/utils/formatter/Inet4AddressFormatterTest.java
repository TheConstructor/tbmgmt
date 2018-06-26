package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by matthias on 18.02.16.
 */
public class Inet4AddressFormatterTest extends AbstractFormatterTest {

    private static final Map<String, Inet4Address> TEST_VALUES = buildTestValues();

    private static final Map<String, Inet4Address> PARSE_TEST_VALUES = buildParseTestValues();

    private static Map<String, Inet4Address> buildTestValues() {
        try {
            return ImmutableMap.<String, Inet4Address>builder()
                    .put("0.0.0.0", (Inet4Address) InetAddress.getByAddress(new byte[]{0, 0, 0, 0}))
                    .put("127.0.0.1", (Inet4Address) InetAddress.getByAddress(new byte[]{127, 0, 0, 1}))
                    .put("192.168.178.42",
                            (Inet4Address) InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, (byte) 178, 42}))
                    .put("255.255.255.255", (Inet4Address) InetAddress.getByAddress(
                            new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255}))
                    .build();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Inet4Address> buildParseTestValues() {
        try {
            return ImmutableMap.<String, Inet4Address>builder()
                    .putAll(TEST_VALUES)
                    .put("localhost", (Inet4Address) InetAddress.getByAddress(new byte[]{127, 0, 0, 1}))
                    .build();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private Inet4AddressFormatter inet4AddressFormatter;

    @Before
    public void createInstance() {
        inet4AddressFormatter = new Inet4AddressFormatter();
    }

    @After
    public void cleanupInstance() {
        inet4AddressFormatter = null;
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testParse() throws Exception {
        AbstractFormatterTest.testParseValues(PARSE_TEST_VALUES, inet4AddressFormatter, null, null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testPrint() throws Exception {
        AbstractFormatterTest.testPrintValues(TEST_VALUES, inet4AddressFormatter, null, "");
    }
}