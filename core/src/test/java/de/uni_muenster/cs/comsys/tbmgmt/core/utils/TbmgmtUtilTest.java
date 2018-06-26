package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by matthias on 17.02.16.
 */
public class TbmgmtUtilTest {

    @Test
    public void testAsContentDisposition() throws Exception {
        Assert.assertEquals(
                "attachment; filename=\"GünesExperiment.xml\"; filename*=UTF-8''G%C3%BCne%C5%9FExperiment.xml",
                TbmgmtUtil.asContentDisposition("GüneşExperiment.xml"));
    }

    @Test
    public void testAddressAsInt() throws Exception {
        innerTestAddressAsInt("192.0.2.91", "C000025B", new byte[]{(byte) 192, 0, 2, 91});
        innerTestAddressAsInt("255.255.255.255", "FFFFFFFF",
                new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
        innerTestAddressAsInt("0.0.0.0", "00000000", new byte[]{0, 0, 0, 0});
    }

    public static void innerTestAddressAsInt(final String message, final String expected, final byte[] addr)
            throws UnknownHostException {
        Assert.assertEquals(message, expected,
                String.format("%08X", TbmgmtUtil.addressAsInt((Inet4Address) InetAddress.getByAddress(addr))));
    }
}