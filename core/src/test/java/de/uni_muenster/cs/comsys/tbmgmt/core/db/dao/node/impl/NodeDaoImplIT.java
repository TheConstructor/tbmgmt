package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbIntegrationTestSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.TestbedDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import static de.uni_muenster.cs.comsys.tbmgmt.test_support.FeatureIs.feature;
import static de.uni_muenster.cs.comsys.tbmgmt.test_support.FeatureIs.featureIsEqualTo;
import static de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil.elementsAreMatching;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * Created by matthias on 18.02.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DbIntegrationTestSpringConfig.class})
public class NodeDaoImplIT {

    @Autowired
    private NodeDao nodeDao;
    
    @Autowired
    private TestbedDao testbedDao;
    
    @Autowired
    private NodeTypeDao nodeTypeDao;
    
    @Autowired
    private InterfaceTypeDao interfaceTypeDao;

    @Test
    @Transactional
    @Rollback
    public void testPersistFind() throws Exception {
        final Node node = new Node();
        node.setTestbed(testbedDao.getByName("Testbed"));
        node.setType(nodeTypeDao.getByName("virtual"));
        final NodeInterface nodeInterface = new NodeInterface();
        nodeInterface.setNode(node);
        node.getInterfaces().add(nodeInterface);
        nodeInterface.setType(interfaceTypeDao.getByName("virtual"));
        final Inet4Address ipv4Address = (Inet4Address) InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        nodeInterface.setIpv4Address(ipv4Address);
        final Inet6Address ipv6Address =
                (Inet6Address) InetAddress.getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
        nodeInterface.setIpv6Address(ipv6Address);
        final MacAddress macAddress = new MacAddress("01-02-03-04-05-06");
        nodeInterface.setMacAddress(macAddress);

        nodeDao.persist(node);
        Assert.assertThat(node, feature("id", Node::getId, CoreMatchers.notNullValue()));
        nodeDao.getEntityManager().flush();

        final Node node1 = nodeDao.find(node.getId());
        nodeDao.getEntityManager().refresh(node1);
        Assert.assertThat(node1, allOf(CoreMatchers.notNullValue(), feature("interfaces", Node::getInterfaces,
                elementsAreMatching(allOf(featureIsEqualTo("ipv4Address", NodeInterface::getIpv4Address, ipv4Address),
                        featureIsEqualTo("ipv6Address", NodeInterface::getIpv6Address, ipv6Address),
                        featureIsEqualTo("macAddress", NodeInterface::getMacAddress, macAddress))))));
    }
}