package de.uni_muenster.cs.comsys.tbmgmt.test_support;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

import java.util.Map;

/**
 * Created by matthias on 18.02.16.
 */
public class EntryMatcher<K, V> extends TypeSafeMatcher<Map.Entry<? extends K, ? extends V>> {

    private final Matcher<K> keyMatcher;
    private final Matcher<V> valueMatcher;

    public EntryMatcher(final Matcher<K> keyMatcher, final Matcher<V> valueMatcher) {
        this.keyMatcher = keyMatcher;
        this.valueMatcher = valueMatcher;
    }

    @Override
    public boolean matchesSafely(final Map.Entry<? extends K, ? extends V> entry) {
        return keyMatcher.matches(entry.getKey()) && valueMatcher.matches(entry.getValue());
    }

    @Override
    public void describeMismatchSafely(final Map.Entry<? extends K, ? extends V> entry,
                                       final Description mismatchDescription) {
        mismatchDescription.appendValue(entry.getKey()).appendText("=").appendValue(entry.getValue());
    }

    @Override
    public void describeTo(final Description description) {
        description.appendDescriptionOf(keyMatcher).appendText("=").appendDescriptionOf(valueMatcher);
    }

    public static <K, V> Matcher<Map.Entry<? extends K, ? extends V>> entryMatching(final K key, final V value) {
        return new EntryMatcher<>(IsEqual.equalTo(key), IsEqual.equalTo(value));
    }

    public static <K, V> Matcher<Map.Entry<? extends K, ? extends V>> entryMatching(final K key,
                                                                                    final Matcher<V> value) {
        return new EntryMatcher<>(IsEqual.equalTo(key), value);
    }
}
