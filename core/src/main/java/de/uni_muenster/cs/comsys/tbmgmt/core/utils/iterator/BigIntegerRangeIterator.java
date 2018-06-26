package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import java.math.BigInteger;

/**
 * Created by matthias on 27.02.16.
 */
public class BigIntegerRangeIterator extends RangeIterator<BigInteger, String> {

    public BigIntegerRangeIterator(final BigInteger start, final BigInteger stepping, final BigInteger end) {
        super(start, stepping, end);
    }

    @Override
    protected BigInteger getZero() {
        return BigInteger.ZERO;
    }

    @Override
    protected String getValue(final BigInteger bigInteger) {
        return bigInteger.toString();
    }

    @Override
    protected BigInteger add(final BigInteger a, final BigInteger b) {
        return a.add(b);
    }
}
