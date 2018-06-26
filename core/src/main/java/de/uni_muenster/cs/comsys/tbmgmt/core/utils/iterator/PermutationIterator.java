package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Iterates over all permutations of values in the {@link Iterable}s.
 * <p><strong>This class assumes that each {@link Iterable} will return an {@link Iterator} over the same
 * elements every time {@link Iterable#iterator()} is called</strong></p>
 * <p><strong>For empty Iterables the output will never contain an entry, otherwise there is always an
 * entry by the same key.</strong></p>
 */
public class PermutationIterator<K, V> implements Iterator<Map<K, V>> {
    /**
     * Keys in original order - List to preserver this order
     */
    private final List<K> keys;
    /**
     * The Iterables on which this is based
     */
    private final Map<K, ? extends Iterable<? extends V>> iterables;
    /**
     * Currently used Iterators
     */
    private final Map<K, Iterator<? extends V>> iterators;
    /**
     * values is a LinkedHashMap so the entries have the same key-order as keys
     */
    private LinkedHashMap<K, V> values;

    public PermutationIterator(final Map<K, ? extends Iterable<? extends V>> iterables) {
        this.keys = new ArrayList<>(iterables.keySet());
        this.iterables = new HashMap<>(iterables);
        this.iterators = new HashMap<>(iterables.size());
        for (final Map.Entry<K, ? extends Iterable<? extends V>> entry : this.iterables.entrySet()) {
            iterators.put(entry.getKey(), entry.getValue().iterator());
        }
        this.values = null;
    }

    @Override
    public boolean hasNext() {
        // this makes aus return an empty map if all iterators immediately fail or nor iterator was provided
        if (values == null) {
            return true;
        }
        // We can generate another permutation as long as there is one Iterator which hasNext()
        for (final Iterator<? extends V> iterator : iterators.values()) {
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // First call to next(); for second call values can not be empty as hasNext would have returned false if no
        // iterator can provide a value
        if (values == null) {
            values = new LinkedHashMap<>(keys.size());
            for (final K key : keys) {
                putFirstValue(key, iterators.get(key));
            }
            return new LinkedHashMap<>(values);
        }

        // Run up to the first Iterator, which hasNext(), regenerate others
        for (final K key : keys) {
            final Iterator<? extends V> iterator = iterators.get(key);
            if (iterator.hasNext()) {
                values.put(key, iterator.next());
                break;
            }
            final Iterator<? extends V> newIterator = iterables.get(key).iterator();
            iterators.put(key, newIterator);
            putFirstValue(key, newIterator);
        }

        return new LinkedHashMap<>(values);
    }

    private V putFirstValue(final K key, final Iterator<? extends V> iterator) {
        //noinspection IfMayBeConditional
        if (iterator.hasNext()) {
            return values.put(key, iterator.next());
        } else {
            return values.remove(key);
        }
    }
}
