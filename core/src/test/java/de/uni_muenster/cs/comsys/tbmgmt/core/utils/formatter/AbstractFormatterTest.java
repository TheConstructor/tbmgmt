package de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter;

import org.junit.Assert;
import org.springframework.format.Formatter;

import java.util.Locale;
import java.util.Map;

/**
 * Created by matthias on 18.02.16.
 */
public abstract class AbstractFormatterTest {

    protected static <V> void testParseValues(final Map<String, ? extends V> testValues, final Formatter<V> formatter,
                                              final Locale locale, final V nullValue) throws Exception {
        for (final Map.Entry<String, ? extends V> entry : testValues.entrySet()) {
            try {
                Assert.assertEquals(entry.getKey(), entry.getValue(), formatter.parse(entry.getKey(), locale));
            } catch (final Exception e) {
                throw new Exception("Exception parsing \"" + entry.getKey() + "\"", e);
            }
        }
        Assert.assertEquals("null", nullValue, formatter.parse(null, locale));
    }

    protected static <V> void testPrintValues(final Map<String, ? extends V> testValues, final Formatter<V> formatter,
                                              final Locale locale, final String nullValue) throws Exception {
        for (final Map.Entry<String, ? extends V> entry : testValues.entrySet()) {
            try {
                Assert.assertEquals(entry.getKey(), entry.getKey(), formatter.print(entry.getValue(), locale));
            } catch (final Exception e) {
                throw new Exception("Exception parsing \"" + entry.getKey() + "\"", e);
            }
        }
        Assert.assertEquals("null", nullValue, formatter.print(null, locale));
    }
}
