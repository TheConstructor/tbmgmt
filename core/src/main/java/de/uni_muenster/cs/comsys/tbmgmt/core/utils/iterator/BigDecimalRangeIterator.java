package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import java.math.BigDecimal;

/**
 * Created by matthias on 27.02.16.
 */
public class BigDecimalRangeIterator extends RangeIterator<BigDecimal, String> {

    public BigDecimalRangeIterator(final BigDecimal start, final BigDecimal stepping, final BigDecimal end) {
        super(start, stepping, end);
    }

    @Override
    protected BigDecimal getZero() {
        return BigDecimal.ZERO;
    }

    @Override
    protected String getValue(final BigDecimal bigDecimal) {
        return bigDecimal.toPlainString();
    }

    @Override
    protected BigDecimal add(final BigDecimal a, final BigDecimal b) {
        return a.add(b);
    }
}
