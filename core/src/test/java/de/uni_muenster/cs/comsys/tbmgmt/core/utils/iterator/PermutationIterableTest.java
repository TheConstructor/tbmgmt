package de.uni_muenster.cs.comsys.tbmgmt.core.utils.iterator;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.collection.IsMapWithSize;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static de.uni_muenster.cs.comsys.tbmgmt.test_support.EntryMatcher.entryMatching;
import static de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil.isMapContaining;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

/**
 * Created by matthias on 27.02.16.
 */
public class PermutationIterableTest {

    @Test
    public void testValueGeneration() {
        Assert.assertThat(new PermutationIterable<>(Collections.emptyMap()),
                containsInAnyOrder(IsMapWithSize.aMapWithSize(0)));

        Assert.assertThat(new PermutationIterable<>(ImmutableMap.of("key1", Collections.emptyList())),
                containsInAnyOrder(IsMapWithSize.aMapWithSize(0)));

        Assert.assertThat(new PermutationIterable<>(ImmutableMap.of("key1", Arrays.asList(1, 2, 3))),
                containsInAnyOrder(isMapContaining(entryMatching("key1", 1)), isMapContaining(entryMatching("key1", 2)),
                        isMapContaining(entryMatching("key1", 3))));

        Assert.assertThat(new PermutationIterable<>(
                        ImmutableMap.of("key1", Arrays.asList(1, 2, 3), "key2", Collections.emptyList())),
                containsInAnyOrder(isMapContaining(entryMatching("key1", 1)), isMapContaining(entryMatching("key1", 2)),
                        isMapContaining(entryMatching("key1", 3))));

        Assert.assertThat(new PermutationIterable<>(
                        ImmutableMap.of("key1", Collections.emptyList(), "key2", Arrays.asList(1, 2, 3))),
                containsInAnyOrder(isMapContaining(entryMatching("key2", 1)), isMapContaining(entryMatching("key2", 2)),
                        isMapContaining(entryMatching("key2", 3))));

        Assert.assertThat(new PermutationIterable<>(
                        ImmutableMap.of("key1", Collections.emptyList(), "key2", Collections.emptyList())),
                containsInAnyOrder(IsMapWithSize.aMapWithSize(0)));

        Assert.assertThat(new PermutationIterable<>(
                        ImmutableMap.of("key1", Arrays.asList(1, 2, 3), "key2", Arrays.asList(3, 4, 5))),
                containsInAnyOrder(isMapContaining(entryMatching("key1", 1), entryMatching("key2", 3)),
                        isMapContaining(entryMatching("key1", 2), entryMatching("key2", 3)),
                        isMapContaining(entryMatching("key1", 3), entryMatching("key2", 3)),
                        isMapContaining(entryMatching("key1", 1), entryMatching("key2", 4)),
                        isMapContaining(entryMatching("key1", 2), entryMatching("key2", 4)),
                        isMapContaining(entryMatching("key1", 3), entryMatching("key2", 4)),
                        isMapContaining(entryMatching("key1", 1), entryMatching("key2", 5)),
                        isMapContaining(entryMatching("key1", 2), entryMatching("key2", 5)),
                        isMapContaining(entryMatching("key1", 3), entryMatching("key2", 5))));
    }
}