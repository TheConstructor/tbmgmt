package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Created by matthias on 30.12.15.
 */
public class TranslitTest {

    public static final String ISO_8859_1_0x0 = "\000\001\002\003\004\005\006\007\010\t\n\013\014\r\016\017";
    public static final String ISO_8859_1_0x1 = "\020\021\022\023\024\025\026\027\030\031\032\033\034\035\036\037";
    public static final String ISO_8859_1_0x2 = " !\"#$%&'()*+,-./";
    public static final String ISO_8859_1_0x3 = "0123456789:;<=>?";
    public static final String ISO_8859_1_0x4 = "@ABCDEFGHIJKLMNO";
    public static final String ISO_8859_1_0x5 = "PQRSTUVWXYZ[\\]^_";
    public static final String ISO_8859_1_0x6 = "`abcdefghijklmno";
    public static final String ISO_8859_1_0x7 = "pqrstuvwxyz{|}~\177";
    public static final String ISO_8859_1_0x8 = "\200\201\202\203\204\205\206\207\210\211\212\213\214\215\216\217";
    public static final String ISO_8859_1_0x8_AS_ASCII = "";
    public static final String ISO_8859_1_0x9 = "\220\221\222\223\224\225\226\227\230\231\232\233\234\235\236\237";
    public static final String ISO_8859_1_0x9_AS_ASCII = "";
    public static final String ISO_8859_1_0xA = "\240¡¢£¤¥¦§¨©ª«¬\255®¯";
    public static final String ISO_8859_1_0xA_AS_ASCII = " \"a ";
    public static final String ISO_8859_1_0xB = "°±²³´µ¶·¸¹º»¼½¾¿";
    public static final String ISO_8859_1_0xB_AS_ASCII = "23  1o1/41/23/4";
    public static final String ISO_8859_1_0xC = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏ";
    public static final String ISO_8859_1_0xC_AS_ASCII = "AAAAAAAECEEEEIIII";
    public static final String ISO_8859_1_0xD = "ÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞß";
    public static final String ISO_8859_1_0xD_AS_ASCII = "DNOOOOO*OUUUUYTHss";
    public static final String ISO_8859_1_0xE = "àáâãäåæçèéêëìíîï";
    public static final String ISO_8859_1_0xE_AS_ASCII = "aaaaaaaeceeeeiiii";
    public static final String ISO_8859_1_0xF = "ðñòóôõö÷øùúûüýþÿ";
    public static final String ISO_8859_1_0xF_AS_ASCII = "dnooooo/ouuuuythy";

    public static final String ISO_8859_1 =
            ISO_8859_1_0x0 + ISO_8859_1_0x1 + ISO_8859_1_0x2 + ISO_8859_1_0x3 + ISO_8859_1_0x4 + ISO_8859_1_0x5
                    + ISO_8859_1_0x6 + ISO_8859_1_0x7 + ISO_8859_1_0x8 + ISO_8859_1_0x9 + ISO_8859_1_0xA
                    + ISO_8859_1_0xB + ISO_8859_1_0xC + ISO_8859_1_0xD + ISO_8859_1_0xE + ISO_8859_1_0xF;

    @Test
    public void testToCharset() throws Exception {
        // Translit should not touch any ISO-8859-1 character given ISO-8859-1 or UTF-8 as charsets
        Assert.assertEquals(ISO_8859_1, Translit.toCharset(ISO_8859_1, StandardCharsets.ISO_8859_1));
        Assert.assertEquals(ISO_8859_1, Translit.toCharset(ISO_8859_1, StandardCharsets.UTF_8));
        // Translit should only touch ISO-8859-1 character greater 0x7f given US-ASCII as charset
        Assert.assertEquals(ISO_8859_1_0x0, Translit.toCharset(ISO_8859_1_0x0, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x1, Translit.toCharset(ISO_8859_1_0x1, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x2, Translit.toCharset(ISO_8859_1_0x2, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x3, Translit.toCharset(ISO_8859_1_0x3, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x4, Translit.toCharset(ISO_8859_1_0x4, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x5, Translit.toCharset(ISO_8859_1_0x5, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x6, Translit.toCharset(ISO_8859_1_0x6, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x7, Translit.toCharset(ISO_8859_1_0x7, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x8_AS_ASCII, Translit.toCharset(ISO_8859_1_0x8, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0x9_AS_ASCII, Translit.toCharset(ISO_8859_1_0x9, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xA_AS_ASCII, Translit.toCharset(ISO_8859_1_0xA, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xB_AS_ASCII, Translit.toCharset(ISO_8859_1_0xB, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xC_AS_ASCII, Translit.toCharset(ISO_8859_1_0xC, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xD_AS_ASCII, Translit.toCharset(ISO_8859_1_0xD, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xE_AS_ASCII, Translit.toCharset(ISO_8859_1_0xE, StandardCharsets.US_ASCII));
        Assert.assertEquals(ISO_8859_1_0xF_AS_ASCII, Translit.toCharset(ISO_8859_1_0xF, StandardCharsets.US_ASCII));
        // Translit should behave well when given the characters which are contained in ISO-8859-15 and the
        // characters of ISO-8859-1 they replace
        Assert.assertEquals("EURSsZzOEoeY¤¦¨´¸¼½¾",
                Translit.toCharset("€ŠšŽžŒœŸ¤¦¨´¸¼½¾", StandardCharsets.ISO_8859_1));
    }
}