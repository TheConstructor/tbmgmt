package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by matthias on 18.02.16.
 */
public class Inet6AddressFormatterTest extends AbstractFormatterTest {

    private static final Map<String, Inet6Address> TEST_VALUES = buildTestValues();

    private static final Map<String, Inet6Address> PARSE_TEST_VALUES = buildParseTestValues();

    private static Map<String, Inet6Address> buildTestValues() {
        try {
            return ImmutableMap.<String, Inet6Address>builder()
                    .put("0:0:0:0:0:0:0:0", (Inet6Address) InetAddress.getByAddress(
                            new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}))
                    .put("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", (Inet6Address) InetAddress.getByAddress(
                            new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}))
                    .build();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, Inet6Address> buildParseTestValues() {
        try {
            return ImmutableMap.<String, Inet6Address>builder()
                    .putAll(TEST_VALUES)
                    .put("[0:0:0:0:0:0:0:0]", (Inet6Address) InetAddress.getByAddress(
                            new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}))
                    .put("[ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff]", (Inet6Address) InetAddress.getByAddress(
                            new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                                    (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}))
                    .put("localhost", (Inet6Address) InetAddress.getByAddress(
                            new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                    .build();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private Inet6AddressFormatter inet6AddressFormatter;

    @Before
    public void createInstance() {
        inet6AddressFormatter = new Inet6AddressFormatter();
    }

    @After
    public void cleanupInstance() {
        inet6AddressFormatter = null;
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testParse() throws Exception {
        AbstractFormatterTest.testParseValues(PARSE_TEST_VALUES, inet6AddressFormatter, null, null);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testPrint() throws Exception {
        AbstractFormatterTest.testPrintValues(TEST_VALUES, inet6AddressFormatter, null, "");
    }
}