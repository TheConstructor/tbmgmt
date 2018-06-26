package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import org.junit.Assert;
import org.junit.Test;

/**
 Created by matthias on 07.10.15.
 */
public class MacAddressTest {

    @Test
    public void testFromString() throws Exception {
        final MacAddress macAddress1 = new MacAddress("00-26-B9-9B-61-BF");
        Assert.assertArrayEquals("Mac1", new byte[]{0x00, 0x26, (byte) 0xB9, (byte) 0x9B, 0x61, (byte) 0xBF},
                macAddress1.getAddress());
        Assert.assertEquals("Mac1", "00-26-B9-9B-61-BF", macAddress1.toString());
        Assert.assertEquals("Mac1", "00:26:B9:9B:61:BF", macAddress1.getColonizedString());

        final MacAddress macAddress2 = new MacAddress("42:12:36:dd:fd:cd");
        Assert.assertArrayEquals("Mac2", new byte[]{0x42, 0x12, 0x36, (byte) 0xdd, (byte) 0xfd, (byte) 0xcd},
                macAddress2.getAddress());
        Assert.assertEquals("Mac2", "42-12-36-DD-FD-CD", macAddress2.toString());
        Assert.assertEquals("Mac2", "42:12:36:DD:FD:CD", macAddress2.getColonizedString());

        final MacAddress macAddress3 = new MacAddress("");
        Assert.assertArrayEquals("Mac3", new byte[]{0, 0, 0, 0, 0, 0}, macAddress3.getAddress());
        Assert.assertEquals("Mac3", "", macAddress3.toString());
        Assert.assertEquals("Mac3", "", macAddress3.getColonizedString());

        try {
            new MacAddress((String) null);
            Assert.fail("No Exception");
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals("Exception", "Provided null as address", e.getMessage());
        }
    }

    @Test
    public void testFromArray() throws Exception {
        final MacAddress macAddress1 =
                new MacAddress(new byte[]{0x00, 0x26, (byte) 0xB9, (byte) 0x9B, 0x61, (byte) 0xBF});
        Assert.assertArrayEquals("Mac1", new byte[]{0x00, 0x26, (byte) 0xB9, (byte) 0x9B, 0x61, (byte) 0xBF},
                macAddress1.getAddress());
        Assert.assertEquals("Mac1", "00-26-B9-9B-61-BF", macAddress1.toString());
        Assert.assertEquals("Mac1", "00:26:B9:9B:61:BF", macAddress1.getColonizedString());

        final MacAddress macAddress2 =
                new MacAddress(new byte[]{0x42, 0x12, 0x36, (byte) 0xdd, (byte) 0xfd, (byte) 0xcd});
        Assert.assertArrayEquals("Mac2", new byte[]{0x42, 0x12, 0x36, (byte) 0xdd, (byte) 0xfd, (byte) 0xcd},
                macAddress2.getAddress());
        Assert.assertEquals("Mac2", "42-12-36-DD-FD-CD", macAddress2.toString());
        Assert.assertEquals("Mac2", "42:12:36:DD:FD:CD", macAddress2.getColonizedString());

        final MacAddress macAddress3 = new MacAddress(new byte[0]);
        Assert.assertArrayEquals("Mac3", new byte[6], macAddress3.getAddress());
        Assert.assertEquals("Mac3", "00-00-00-00-00-00", macAddress3.toString());
        Assert.assertEquals("Mac3", "00:00:00:00:00:00", macAddress3.getColonizedString());

        try {
            new MacAddress((String) null);
            Assert.fail("No Exception");
        } catch (final IllegalArgumentException e) {
            Assert.assertEquals("Exception", "Provided null as address", e.getMessage());
        }
    }
}