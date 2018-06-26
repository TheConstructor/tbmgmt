package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by matthias on 27.02.16.
 */
public abstract class RangeIterator<N extends Number & Comparable<N>, V> implements Iterator<V> {
    private N next;
    private final N stepping;
    private final N end;
    private final boolean startLessOrEqualEnd;
    /**
     * Equality as determined by {@link Comparable#compareTo(Object)} as {@link BigDecimal#equals(Object)} takes
     * scale into account and we want to know if addition of {@link #stepping} changes the value regardless of scale
     */
    private final boolean steppingEqualToZero;
    private boolean reachedEnd = false;

    public RangeIterator(final N start, final N stepping, final N end) {
        next = start;
        this.stepping = stepping;
        this.end = end;

        final int startComparedToEnd = start.compareTo(end);
        final int steppingComparedToZero = stepping.compareTo(getZero());
        startLessOrEqualEnd = startComparedToEnd <= 0;
        steppingEqualToZero = steppingComparedToZero == 0;
        if (steppingEqualToZero) {
            if (startComparedToEnd != 0) {
                throw new IllegalArgumentException("For start != end a non-zero stepping is required");
            }
        } else if (startLessOrEqualEnd == (steppingComparedToZero <= 0)) {
            throw new IllegalArgumentException("start < end is required for positive stepping and vice-versa");
        }
    }

    @Override
    public boolean hasNext() {
        final int nextComparedToEnd = next.compareTo(end);
        if (nextComparedToEnd != 0 && nextComparedToEnd <= 0 != startLessOrEqualEnd) {
            reachedEnd = true;
        }
        return !reachedEnd;
    }

    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final V value = getValue(next);
        if (steppingEqualToZero) {
            reachedEnd = true;
        } else {
            next = add(next, stepping);
        }
        return value;
    }

    protected abstract N getZero();

    protected abstract V getValue(N n);

    protected abstract N add(N a, N b);
}
