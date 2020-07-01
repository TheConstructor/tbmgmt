package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbIntegrationTestSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeType;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.FeatureIs;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by matthias on 18.02.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DbIntegrationTestSpringConfig.class})
public class NodeTypeDaoImplIT {

    @Autowired
    private NodeTypeDao nodeTypeDao;

    @Test
    public void testGetNames() throws Exception {
        Assert.assertThat(nodeTypeDao.getNames(), TestUtil.elementsAreEqual("router", "virtual"));
    }

    @Test
    public void testGetByName() throws Exception {
        Assert.assertThat(nodeTypeDao.getByName("router"), CoreMatchers.allOf(CoreMatchers.instanceOf(NodeType.class),
                FeatureIs.featureIsEqualTo("name", "name", NodeType::getName, "router")));
        Assert.assertThat(nodeTypeDao.getByName("virtual"), CoreMatchers.allOf(CoreMatchers.instanceOf(NodeType.class),
                FeatureIs.featureIsEqualTo("name", "name", NodeType::getName, "virtual")));
    }
}