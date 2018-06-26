package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import org.junit.Assert;
import org.junit.Test;

import static de.uni_muenster.cs.comsys.tbmgmt.test_support.EntryMatcher.entryMatching;
import static de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil.isMapContaining;

/**
 * Created by matthias on 18.02.16.
 */
public class EnumUtilTest {

    private enum TestEnum {
        One(1), Two(2), Three(3);

        private final int i;

        TestEnum(final int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }
    }

    @Test
    public void testGetNameValueMap() throws Exception {
        Assert.assertThat(EnumUtil.getNameValueMap(TestEnum.class),
                isMapContaining(entryMatching("One", TestEnum.One), entryMatching("Two", TestEnum.Two),
                        entryMatching("Three", TestEnum.Three)));
    }

    @Test
    public void testGetValueMap() throws Exception {
        Assert.assertThat(EnumUtil.getValueMap(TestEnum::getI, TestEnum.class),
                isMapContaining(entryMatching(1, TestEnum.One), entryMatching(2, TestEnum.Two),
                        entryMatching(3, TestEnum.Three)));
    }
}