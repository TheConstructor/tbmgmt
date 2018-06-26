package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by matthias on 27.02.16.
 */
public class PermutationIterable<K, V> implements Iterable<Map<K, V>> {

    private final LinkedHashMap<K, ? extends Iterable<? extends V>> iterables;

    public PermutationIterable(final Map<K, ? extends Iterable<? extends V>> iterables) {
        this.iterables = new LinkedHashMap<>(iterables);
    }

    @Override
    public Iterator<Map<K, V>> iterator() {
        return new PermutationIterator<>(iterables);
    }
}
