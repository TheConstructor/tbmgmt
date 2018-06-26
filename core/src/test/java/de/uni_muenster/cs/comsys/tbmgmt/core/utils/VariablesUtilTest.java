package de.uni_muenster.cs.comsys.tbmgmt.core.utils;

import com.google.common.collect.ImmutableMap;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroupBuilder;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeBuilder;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterfaceBuilder;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.EntryMatcher;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil;
import org.hamcrest.collection.IsMapWithSize;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by matthias on 28.02.16.
 */
public class VariablesUtilTest {

    @Test
    public void testReplaceVariables() throws Exception {
        Assert.assertEquals("", VariablesUtil.replaceVariables("", ImmutableMap.of()));
        Assert.assertEquals("{one}", VariablesUtil.replaceVariables("{one}", ImmutableMap.of()));
        Assert.assertEquals("", VariablesUtil.replaceVariables("", ImmutableMap.of("one", "two")));
        Assert.assertEquals("two", VariablesUtil.replaceVariables("{one}", ImmutableMap.of("one", "two")));
        Assert.assertEquals("sometwoelse",
                VariablesUtil.replaceVariables("some{one}else", ImmutableMap.of("one", "two")));
        Assert.assertEquals("two one two",
                VariablesUtil.replaceVariables("{one} one {one}", ImmutableMap.of("one", "two")));
        Assert.assertEquals("{two} {one} {two}",
                VariablesUtil.replaceVariables("{one} {two} {one}", ImmutableMap.of("one", "{two}", "two", "{one}")));
    }

    @Test
    public void testExtractNodeGroupReferences() throws Exception {
        final InterfaceType wired = new InterfaceType();
        wired.setName("wired");
        final InterfaceType wireless = new InterfaceType();
        wireless.setName("wireless");
        final InterfaceType virtual = new InterfaceType();
        virtual.setName("virtual");

        final ExperimentNodeGroup nodeGroup1 = new ExperimentNodeGroupBuilder()
                .name("foo")
                .node(new NodeBuilder()
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("eth0")
                                .type(wired)
                                .macAddress("00:00:00:00:00:00")
                                .ipv4Address("1.1.1.1")
                                .ipv6Address("1:1:1:1:1:1:1:1")
                                .controlledOverThisConnection(true)
                                .build())
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("wlan0")
                                .type(wireless)
                                .macAddress("01:00:00:00:00:00")
                                .ipv4Address("2.1.1.1")
                                .ipv6Address("2:1:1:1:1:1:1:1")
                                .controlledOverThisConnection(false)
                                .build())
                        .build())
                .node(new NodeBuilder()
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("eth0")
                                .type(wired)
                                .macAddress("00:00:00:00:00:02")
                                .ipv4Address("1.1.1.2")
                                .ipv6Address("1:1:1:1:1:1:1:2")
                                .controlledOverThisConnection(true)
                                .build())
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("wlan0")
                                .type(wireless)
                                .macAddress("01:00:00:00:00:02")
                                .ipv4Address("2.1.1.2")
                                .ipv6Address("2:1:1:1:1:1:1:2")
                                .controlledOverThisConnection(false)
                                .build())
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("virt0")
                                .type(virtual)
                                .macAddress("03:00:00:00:00:02")
                                .ipv4Address("3.1.1.2")
                                .ipv6Address("3:1:1:1:1:1:1:2")
                                .controlledOverThisConnection(false)
                                .build())
                        .build())
                .node(new NodeBuilder()
                        .nodeInterface(new NodeInterfaceBuilder()
                                .name("virt0")
                                .type(virtual)
                                .macAddress("03:00:00:00:00:03")
                                .ipv4Address("3.1.1.3")
                                .ipv6Address("3:1:1:1:1:1:1:3")
                                .controlledOverThisConnection(false)
                                .build())
                        .build())
                .build();

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("", s -> "ng1".equals(s) ? nodeGroup1 : null),
                IsMapWithSize.anEmptyMap());

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng2}", s -> "ng1".equals(s) ? nodeGroup1 : null),
                IsMapWithSize.anEmptyMap());

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng1}", s -> "ng1".equals(s) ? nodeGroup1 : null),
                TestUtil.isMapContaining(EntryMatcher.entryMatching("ng1",
                        TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2", "3.1.1.2", "3.1.1.3"))));

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("kj{sadlkajsh{ng1}ökdsahfakjds{ng2..ipv6}",
                s -> "ng1".equals(s) ? nodeGroup1 : null), TestUtil.isMapContaining(EntryMatcher.entryMatching("ng1",
                TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2", "3.1.1.2", "3.1.1.3"))));

        Assert.assertThat(
                VariablesUtil.extractNodeGroupReferences("{ng1..ipv4}", s -> "ng1".equals(s) ? nodeGroup1 : null),
                TestUtil.isMapContaining(EntryMatcher.entryMatching("ng1..ipv4",
                        TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2", "3.1.1.2", "3.1.1.3"))));

        Assert.assertThat(
                VariablesUtil.extractNodeGroupReferences("{ng1.wlan0}", s -> "ng1".equals(s) ? nodeGroup1 : null),
                TestUtil.isMapContaining(
                        EntryMatcher.entryMatching("ng1.wlan0", TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2"))));

        Assert.assertThat(
                VariablesUtil.extractNodeGroupReferences("{ng1.*wireless}", s -> "ng1".equals(s) ? nodeGroup1 : null),
                TestUtil.isMapContaining(
                        EntryMatcher.entryMatching("ng1.*wireless", TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2"))));

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng1.*wireless.ipv4}",
                s -> "ng1".equals(s) ? nodeGroup1 : null), TestUtil.isMapContaining(
                EntryMatcher.entryMatching("ng1.*wireless.ipv4", TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2"))));

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng1.*wireless.bs}",
                s -> "ng1".equals(s) ? nodeGroup1 : null), TestUtil.isMapContaining(
                EntryMatcher.entryMatching("ng1.*wireless.bs", TestUtil.elementsAreEqual("2.1.1.1", "2.1.1.2"))));

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng1.*wireless.ipv6}",
                s -> "ng1".equals(s) ? nodeGroup1 : null), TestUtil.isMapContaining(
                EntryMatcher.entryMatching("ng1.*wireless.ipv6",
                        TestUtil.elementsAreEqual("2:1:1:1:1:1:1:1", "2:1:1:1:1:1:1:2"))));

        Assert.assertThat(VariablesUtil.extractNodeGroupReferences("{ng1.*wireless.mac}",
                s -> "ng1".equals(s) ? nodeGroup1 : null), TestUtil.isMapContaining(
                EntryMatcher.entryMatching("ng1.*wireless.mac",
                        TestUtil.elementsAreEqual("01-00-00-00-00-00", "01-00-00-00-00-02"))));
    }
}