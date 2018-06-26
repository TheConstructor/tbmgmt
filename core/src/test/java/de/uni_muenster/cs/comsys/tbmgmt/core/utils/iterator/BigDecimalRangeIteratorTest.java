package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by matthias on 27.02.16.
 */
public class BigDecimalRangeIteratorTest {

    @Test
    public void testValueGeneration() {
        Assert.assertThat(
                TestUtil.asList(new BigDecimalRangeIterator(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)),
                TestUtil.elementsAreEqualInOrder("0"));

        Assert.assertThat(
                TestUtil.asList(new BigDecimalRangeIterator(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(5))),
                TestUtil.elementsAreEqualInOrder("0", "1", "2", "3", "4", "5"));
        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(BigDecimal.valueOf(5), BigDecimal.ONE.negate(), BigDecimal.ZERO)),
                TestUtil.elementsAreEqualInOrder("5", "4", "3", "2", "1", "0"));

        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(BigDecimal.ZERO, BigDecimal.valueOf(2), BigDecimal.valueOf(5))),
                TestUtil.elementsAreEqualInOrder("0", "2", "4"));
        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(BigDecimal.valueOf(5), BigDecimal.valueOf(-2), BigDecimal.ZERO)),
                TestUtil.elementsAreEqualInOrder("5", "3", "1"));

        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(BigDecimal.ZERO, new BigDecimal("1.1"), BigDecimal.valueOf(5))),
                TestUtil.elementsAreEqualInOrder("0", "1.1", "2.2", "3.3", "4.4"));
        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(BigDecimal.valueOf(5), new BigDecimal("-1.1"), BigDecimal.ZERO)),
                TestUtil.elementsAreEqualInOrder("5", "3.9", "2.8", "1.7", "0.6"));

        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(new BigDecimal("0.0"), BigDecimal.ONE, BigDecimal.valueOf(5))),
                TestUtil.elementsAreEqualInOrder("0.0", "1.0", "2.0", "3.0", "4.0", "5.0"));
        Assert.assertThat(TestUtil.asList(
                new BigDecimalRangeIterator(new BigDecimal("5.0"), BigDecimal.ONE.negate(), BigDecimal.ZERO)),
                TestUtil.elementsAreEqualInOrder("5.0", "4.0", "3.0", "2.0", "1.0", "0.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException1() {
        new BigDecimalRangeIterator(new BigDecimal("5"), new BigDecimal("0"), new BigDecimal("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2() {
        new BigDecimalRangeIterator(new BigDecimal("1"), new BigDecimal("0"), new BigDecimal("5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException3() {
        new BigDecimalRangeIterator(new BigDecimal("1"), new BigDecimal("-1"), new BigDecimal("5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException4() {
        new BigDecimalRangeIterator(new BigDecimal("5"), new BigDecimal("1"), new BigDecimal("1"));
    }
}