package de.uni_muenster.cs.comsys.tbmgmt.test_support;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.collection.IsMapWithSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 Created by matthias on 16.03.15.
 */
public final class TestUtil {

    public static <T, V> Matcher<Collection<? extends T>> elementsAreEqual(final Collection<V> expected,
                                                                           final Function<T, V> transform) {
        final int expectedSize = expected.size();
        final Map<V, Integer> valueFrequencies = new HashMap<>(expected.size());
        for (final V v : expected) {
            valueFrequencies.compute(v, (k, f) -> f == null ? 1 : f + 1);
        }
        return CoreMatchers.both(IsCollectionWithSize.<T>hasSize(expectedSize))
                .and(new TypeSafeDiagnosingMatcher<Collection<? extends T>>() {
                    @Override
                    protected boolean matchesSafely(final Collection<? extends T> item,
                                                    final Description mismatchDescription) {
                        final HashMap<V, Integer> remainingValueFrequencies = new HashMap<>(valueFrequencies);
                        for (final T t : item) {
                            final V v = transform.apply(t);
                            if (remainingValueFrequencies.getOrDefault(v, 0) < 1) {
                                mismatchDescription.appendValue(t).appendText(" which was transformed to ")
                                        .appendValue(v).appendText(" appears more than the expected ")
                                        .appendValue(valueFrequencies.getOrDefault(v, 0)).appendText(" times");
                                return false;
                            }
                            remainingValueFrequencies.compute(v, (k, f) -> f == null ? -1 : f - 1);
                        }
                        boolean success = true;
                        for (final Map.Entry<V, Integer> entry : remainingValueFrequencies.entrySet()) {
                            if (!entry.getValue().equals(0)) {
                                if (!success) {
                                    mismatchDescription.appendText(" ");
                                }
                                mismatchDescription.appendValue(entry.getKey()).appendText(" was expected ")
                                        .appendValue(entry.getValue()).appendText(" more times ");
                                success = false;
                            }
                        }
                        return success;
                    }

                    @Override
                    public void describeTo(final Description description) {
                        description.appendText("collection consisting of ").appendValueList("[", ", ", "]", expected);
                    }
                });
    }

    @SafeVarargs
    public static <T> Matcher<Collection<? extends T>> elementsAreEqual(final T... expected) {
        final int expectedSize = expected.length;
        return CoreMatchers.<Collection<? extends T>>both(
                IsIterableContainingInAnyOrder.containsInAnyOrder(expected)).and(
                IsCollectionWithSize.hasSize(expectedSize));
    }

    @SafeVarargs
    public static <T> Matcher<Collection<? extends T>> elementsAreEqualInOrder(final T... expected) {
        final int expectedSize = expected.length;
        return CoreMatchers.<Collection<? extends T>>both(IsIterableContainingInOrder.contains(expected)).and(
                IsCollectionWithSize.hasSize(expectedSize));
    }

    @SafeVarargs
    public static <T, V> Matcher<Collection<? extends T>> elementFeaturesAreEqual(final Function<T, V> transform,
                                                                                  final String featureDescription,
                                                                                  final String featureName,
                                                                                  final V... expected) {
        final ArrayList<Matcher<? super T>> matchers = new ArrayList<>(expected.length);
        for (final V v : expected) {
            matchers.add(FeatureIs.featureIsEqualTo(featureDescription, featureName, transform, v));
        }
        return elementsAreMatching(matchers);
    }

    @SafeVarargs
    public static <T> Matcher<Collection<? extends T>> elementsAreMatching(final Matcher<? super T>... expected) {
        return elementsAreMatching(Arrays.asList(expected));
    }

    public static <T> Matcher<Collection<? extends T>> elementsAreMatching(
            final Collection<Matcher<? super T>> expected) {
        final int expectedSize = expected.size();
        return CoreMatchers.<Collection<? extends T>>both(
                IsIterableContainingInAnyOrder.containsInAnyOrder(expected)).and(
                IsCollectionWithSize.hasSize(expectedSize));
    }

    @SafeVarargs
    public static <K, V> Matcher<Map<? extends K, ? extends V>> isMapContaining(
            final Matcher<? super Map.Entry<? extends K, ? extends V>>... expected) {
        return isMapContaining(Arrays.asList(expected));
    }

    public static <K, V> Matcher<Map<? extends K, ? extends V>> isMapContaining(
            final Collection<Matcher<? super Map.Entry<? extends K, ? extends V>>> expected) {
        final int expectedSize = expected.size();
        return CoreMatchers.both(new MapContainsInAnyOrder<>(expected)).and(IsMapWithSize.aMapWithSize(expectedSize));
    }

    private static class MapContainsInAnyOrder<K, V>
            extends FeatureMatcher<Map<? extends K, ? extends V>, Set<? extends Map.Entry<? extends K, ?
            extends V>>> {
        public MapContainsInAnyOrder(final Collection<Matcher<? super Map.Entry<? extends K, ? extends V>>> expected) {
            super(IsIterableContainingInAnyOrder.containsInAnyOrder(expected), "map containing entries", "entries");
        }

        @Override
        protected Set<? extends Map.Entry<? extends K, ? extends V>> featureValueOf(
                final Map<? extends K, ? extends V> actual) {
            return actual.entrySet();
        }
    }

    public static <T> List<T> asList(Iterator<T> iterator) {
        final List<T> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
