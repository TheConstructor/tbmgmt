package de.uni_muenster.cs.comsys.tbmgmt.web.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

/**
 Created by matthias on 23.04.15.
 */
public class ConvertingList<Inner, Outer> extends AbstractList<Outer> implements Serializable {
    private final List<Inner>            innerList;
    private final Function<Inner, Outer> innerToOuter;
    private final Function<Outer, Inner> outerToInner;

    public ConvertingList(final List<Inner> innerList, final Function<Inner, Outer> innerToOuter,
            final Function<Outer, Inner> outerToInner) {
        this.innerList = innerList;
        this.innerToOuter = innerToOuter;
        this.outerToInner = outerToInner;
    }

    @Override
    public Outer get(final int index) {
        return innerToOuter.apply(innerList.get(index));
    }

    @Override
    public Outer set(final int index, final Outer element) {
        Inner previousNode = innerList.set(index, outerToInner.apply(element));
        if (previousNode != null) {
            return innerToOuter.apply(previousNode);
        } else {
            return null;
        }
    }

    @Override
    public void add(final int index, final Outer element) {
        innerList.add(index, outerToInner.apply(element));
    }

    @Override
    public Outer remove(final int index) {
        Inner previousNode = innerList.remove(index);
        if (previousNode != null) {
            return innerToOuter.apply(previousNode);
        } else {
            return null;
        }
    }

    @Override
    public int size() {
        return innerList.size();
    }

    @Override
    public void clear() {
        innerList.clear();
    }
}
