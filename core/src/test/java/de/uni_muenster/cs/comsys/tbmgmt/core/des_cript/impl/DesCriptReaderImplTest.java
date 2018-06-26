package de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.ExperimentNodeGroup;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentActionBlock;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.DesCriptSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.EvaluationScriptResolverMock;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NameResolverMock;
import de.uni_muenster.cs.comsys.tbmgmt.core.des_cript.NodeNameResolver;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.ExecutionMode;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.NodeRole;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.formatter.InstantFormatter;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.xml.transform.ResourceSource;

import javax.xml.transform.Source;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DesCriptReaderImplTest.class, DesCriptSpringConfig.class})
@TransactionConfiguration(defaultRollback = true)
public class DesCriptReaderImplTest {

    private static final Logger LOG = Logger.getLogger(DesCriptReaderImplTest.class.getName());

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Jaxb2Marshaller jaxb2Marshaller;

    @Autowired
    private InstantFormatter instantFormatter;

    @Bean
    public NodeNameResolver nodeNameResolver() {
        return new NameResolverMock();
    }

    @Bean
    public EvaluationScriptResolver evaluationScriptResolver() {
        return new EvaluationScriptResolverMock();
    }

    @Test
    public void testRead() throws Exception {
        Resource resource = resourceLoader.getResource(
                "classpath:de/uni_muenster/cs/comsys/tbmgmt/core/des_cript/impl/parameter-stepwise.xml");
        Source source = new ResourceSource(resource);

        DesCriptReaderImpl reader = new DesCriptReaderImpl(jaxb2Marshaller, instantFormatter, new NameResolverMock(),
                new EvaluationScriptResolverMock());
        Experiment experiment = reader.read(source);

        assertNotNull(experiment);
        final Duration zeroSeconds = Duration.ofSeconds(0);
        final Duration sixtySeconds = Duration.ofSeconds(60);
        {
            /* General-Block */
            assertEquals("name", experiment.getName(), "opnex meeting - 01");
            assertEquals("description", "", experiment.getDescription());
            assertEquals("startTime", Instant.parse("2009-11-06T14:54:22Z"), experiment.getStartTime());
            assertEquals("duration", zeroSeconds, experiment.getDuration());
            assertEquals("iterations", 1, experiment.getReplications());
            assertEquals("iterationPause", zeroSeconds, experiment.getPauseBetweenReplications());
            assertEquals("sampleInterval", sixtySeconds, experiment.getSampleInterval());
            assertEquals("restartNodes", false, experiment.isRestartNodes());
            assertEquals("lockTestbed", false, experiment.isLockTestbed());
            assertThat("nodeGroups.keys",
                    experiment.getNodeGroups().stream().map(ExperimentNodeGroup::getName).collect(Collectors.toList()),
                    TestUtil.elementsAreEqual("All", "iperf server", "iperf-client"));
            verifyNodeGroup("nodeGroups[All]", experiment.getNodeGroupByName("All"), "All", NodeRole.SERVER, "t9-150",
                    "t9-149", "t9-105", "t9-146", "t9-154", "t9-155", "t9-162", "t9-163", "t9-158", "t9-169", "t9-108",
                    "t9-113", "t9-117", "t9-165", "t9-134", "t9-124", "t9-136", "t9-166", "t9-137");
            verifyNodeGroup("nodeGroups[iperf server]", experiment.getNodeGroupByName("iperf server"), "iperf server",
                    NodeRole.SERVER, "t9-150");
            verifyNodeGroup("nodeGroups[iperf-client]", experiment.getNodeGroupByName("iperf-client"), "iperf-client",
                    NodeRole.CLIENT, "t9-149");
            assertEquals("files", 0, experiment.getFiles().size());
        }
        {
            /* Actions-Block */
            assertEquals("variables.size", 0, experiment.getVariables().size());
            assertThat("actionBlocks[].sequence", experiment.getActionBlocks(), TestUtil.elementFeaturesAreEqual(
                    ExperimentActionBlock::getSequence, "Sequence-Id of ActionBlock", "sequence-id",
                    BigInteger.valueOf(0), BigInteger.valueOf(1)));
            for (ExperimentActionBlock actionBlock : experiment.getActionBlocks()) {
                switch (actionBlock.getSequence().intValueExact()) {
                    case 0:
                        assertEquals("actionsBlocks[0].executionMode", ExecutionMode.PARALLEL,
                                actionBlock.getExecutionMode());
                        assertThat("actionsBlocks[0].actions[].sequence", actionBlock.getActions(),
                                TestUtil.elementFeaturesAreEqual(
                                        ExperimentAction::getSequence, "Sequence-Id of Action", "sequence-id",
                                        BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2)));
                        for (ExperimentAction action : actionBlock.getActions()) {
                            switch (action.getSequence().intValueExact()) {
                                case 0:
                                    assertEquals("actionsBlocks[0].actions[0].command", "sudo ifup wlan0",
                                            action.getCommand());
                                    assertSame("actionsBlocks[0].actions[0].targetedNodeGroup",
                                            experiment.getNodeGroupByName("All"),
                                            action.getTargetedNodeGroup());
                                    assertEquals("actionsBlocks[0].actions[0].startTime", zeroSeconds,
                                            action.getStartOffset());
                                    assertEquals("actionsBlocks[0].actions[0].duration", zeroSeconds,
                                            action.getDuration());
                                    assertEquals("actionsBlocks[0].actions[0].evaluationScript", null,
                                            action.getEvaluationScript());
                                    assertEquals("actionsBlocks[0].actions[0].evaluationParameter", null,
                                            action.getEvaluationParameter());
                                    break;
                                case 1:
                                    assertEquals("actionsBlocks[0].actions[1].command", "sudo iwconfig wlan chan 13",
                                            action.getCommand());
                                    assertSame("actionsBlocks[0].actions[1].targetedNodeGroup",
                                            experiment.getNodeGroupByName("All"),
                                            action.getTargetedNodeGroup());
                                    assertEquals("actionsBlocks[0].actions[1].startTime", zeroSeconds,
                                            action.getStartOffset());
                                    assertEquals("actionsBlocks[0].actions[1].duration", zeroSeconds,
                                            action.getDuration());
                                    assertEquals("actionsBlocks[0].actions[1].evaluationScript", null,
                                            action.getEvaluationScript());
                                    assertEquals("actionsBlocks[0].actions[1].evaluationParameter", null,
                                            action.getEvaluationParameter());
                                    break;
                                case 2:
                                    assertEquals("actionsBlocks[0].actions[2].command",
                                            "sudo /etc/init.d/olsrd restart", action.getCommand());
                                    assertSame("actionsBlocks[0].actions[2].targetedNodeGroup",
                                            experiment.getNodeGroupByName("All"),
                                            action.getTargetedNodeGroup());
                                    assertEquals("actionsBlocks[0].actions[2].startTime", zeroSeconds,
                                            action.getStartOffset());
                                    assertEquals("actionsBlocks[0].actions[2].duration", zeroSeconds,
                                            action.getDuration());
                                    assertEquals("actionsBlocks[0].actions[2].evaluationScript", null,
                                            action.getEvaluationScript());
                                    assertEquals("actionsBlocks[0].actions[2].evaluationParameter", null,
                                            action.getEvaluationParameter());
                                    break;
                            }
                        }
                        break;
                    case 1:
                        assertEquals("actionsBlocks[1].executionMode", ExecutionMode.TIMED,
                                actionBlock.getExecutionMode());
                        assertThat("actionsBlocks[1].actions[].sequence", actionBlock.getActions(),
                                TestUtil.elementFeaturesAreEqual(
                                        ExperimentAction::getSequence, "Sequence-Id of Action", "sequence-id",
                                        BigInteger.valueOf(1), BigInteger.valueOf(2)));
                        for (ExperimentAction action : actionBlock.getActions()) {
                            switch (action.getSequence().intValueExact()) {
                                case 1:
                                    assertEquals("actionsBlocks[1].actions[1].command", "iperf -s",
                                            action.getCommand());
                                    assertSame("actionsBlocks[1].actions[1].targetedNodeGroup",
                                            experiment.getNodeGroupByName("iperf server"),
                                            action.getTargetedNodeGroup());
                                    assertEquals("actionsBlocks[1].actions[1].startTime", zeroSeconds,
                                            action.getStartOffset());
                                    assertEquals("actionsBlocks[1].actions[1].duration", Duration.ofSeconds(120),
                                            action.getDuration());
                                    assertEquals("actionsBlocks[1].actions[1].evaluationScript", Long.valueOf(1),
                                            action.getEvaluationScript().getId());
                                    assertEquals("actionsBlocks[1].actions[1].evaluationParameter",
                                            "opnex-meeting-server", action.getEvaluationParameter());
                                    break;
                                case 2:
                                    assertEquals("actionsBlocks[1].actions[2].command", "iperf -c 192.168.18.1 -t60",
                                            action.getCommand());
                                    assertSame("actionsBlocks[1].actions[2].targetedNodeGroup",
                                            experiment.getNodeGroupByName("iperf-client"),
                                            action.getTargetedNodeGroup());
                                    assertEquals("actionsBlocks[1].actions[2].startTime", Duration.ofSeconds(5),
                                            action.getStartOffset());
                                    assertEquals("actionsBlocks[1].actions[2].duration", Duration.ofSeconds(70),
                                            action.getDuration());
                                    assertEquals("actionsBlocks[1].actions[2].evaluationScript", Long.valueOf(1),
                                            action.getEvaluationScript().getId());
                                    assertEquals("actionsBlocks[1].actions[2].evaluationParameter",
                                            "opnex-meeting-client", action.getEvaluationParameter());
                                    break;
                            }
                        }
                        break;
                }
            }

        }
    }

    private void verifyNodeGroup(String prefix, ExperimentNodeGroup nodeGroup, String groupName, NodeRole role,
            String... nodeNames) {
        assertEquals(prefix + ".name", groupName, nodeGroup.getName());
        assertEquals(prefix + ".role", role, nodeGroup.getRole());
        assertThat(prefix + ".nodes[].name", nodeGroup.getNodes(),
                TestUtil.elementFeaturesAreEqual(Node::getName, "Name of the node", "name", nodeNames));
    }

}