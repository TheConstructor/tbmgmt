package de.uni_muenster.cs.comsys.tbmgmt.test_support;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;

import java.util.function.Function;

/**
 * Created by matthias on 18.02.16.
 */
public class FeatureIs<T, V> extends FeatureMatcher<T, V> {
    private final Function<T, V> transform;

    public FeatureIs(final String featureDescription, final String featureName, final Function<T, V> transform,
                     final Matcher<? super V> valueMatcher) {
        super(valueMatcher, featureDescription, featureName);
        this.transform = transform;
    }

    @Override
    protected V featureValueOf(final T actual) {
        return transform.apply(actual);
    }

    public static <T, V> FeatureIs<T, V> featureIsEqualTo(final String featureName, final Function<T, V> transform,
                                                          final V v) {
        return featureIsEqualTo(featureName, featureName, transform, v);
    }

    public static <T, V> FeatureIs<T, V> featureIsEqualTo(final String featureDescription, final String featureName,
                                                          final Function<T, V> transform, final V v) {
        return new FeatureIs<T, V>(featureDescription, featureName, transform, IsEqual.equalTo(v));
    }

    public static <T, V> FeatureIs<T, V> feature(final String featureName, final Function<T, V> transform,
                                                 final Matcher<? super V> valueMatcher) {
        return feature(featureName, featureName, transform, valueMatcher);
    }

    public static <T, V> FeatureIs<T, V> feature(final String featureDescription, final String featureName,
                                                 final Function<T, V> transform,
                                                 final Matcher<? super V> valueMatcher) {
        return new FeatureIs<T, V>(featureDescription, featureName, transform, valueMatcher);
    }
}
