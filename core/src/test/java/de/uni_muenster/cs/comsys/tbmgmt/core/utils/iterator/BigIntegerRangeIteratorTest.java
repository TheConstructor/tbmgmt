package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by matthias on 27.02.16.
 */
public class BigIntegerRangeIteratorTest {

    @Test
    public void testValueGeneration() {
        Assert.assertThat(
                (Iterable<String>) () -> new BigIntegerRangeIterator(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO),
                IsIterableContainingInOrder.contains("0"));

        Assert.assertThat((Iterable<String>) () -> new BigIntegerRangeIterator(BigInteger.ZERO, BigInteger.ONE,
                BigInteger.valueOf(5)), IsIterableContainingInOrder.contains("0", "1", "2", "3", "4", "5"));
        Assert.assertThat(
                (Iterable<String>) () -> new BigIntegerRangeIterator(BigInteger.valueOf(5), BigInteger.ONE.negate(),
                        BigInteger.ZERO), IsIterableContainingInOrder.contains("5", "4", "3", "2", "1", "0"));

        Assert.assertThat((Iterable<String>) () -> new BigIntegerRangeIterator(BigInteger.ZERO, BigInteger.valueOf(2),
                BigInteger.valueOf(5)), IsIterableContainingInOrder.contains("0", "2", "4"));
        Assert.assertThat(
                (Iterable<String>) () -> new BigIntegerRangeIterator(BigInteger.valueOf(5), BigInteger.valueOf(-2),
                        BigInteger.ZERO), IsIterableContainingInOrder.contains("5", "3", "1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException1() {
        new BigIntegerRangeIterator(new BigInteger("5"), new BigInteger("0"), new BigInteger("1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException2() {
        new BigIntegerRangeIterator(new BigInteger("1"), new BigInteger("0"), new BigInteger("5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException3() {
        new BigIntegerRangeIterator(new BigInteger("1"), new BigInteger("-1"), new BigInteger("5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testException4() {
        new BigIntegerRangeIterator(new BigInteger("5"), new BigInteger("1"), new BigInteger("1"));
    }
}