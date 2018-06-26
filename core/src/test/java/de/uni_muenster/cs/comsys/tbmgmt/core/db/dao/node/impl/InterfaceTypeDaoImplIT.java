package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbIntegrationTestSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.FeatureIs;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.TestUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by matthias on 18.02.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DbIntegrationTestSpringConfig.class})
public class InterfaceTypeDaoImplIT {

    @Autowired
    private InterfaceTypeDao interfaceTypeDao;

    @Test
    public void testGetNames() throws Exception {
        Assert.assertThat(interfaceTypeDao.getNames(), TestUtil.elementsAreEqual("wired", "wireless", "virtual"));
    }

    @Test
    public void testGetByName() throws Exception {
        Assert.assertThat(interfaceTypeDao.getByName("wired"),
                CoreMatchers.allOf(CoreMatchers.instanceOf(InterfaceType.class),
                        FeatureIs.featureIsEqualTo("name", "name", InterfaceType::getName, "wired")));
        Assert.assertThat(interfaceTypeDao.getByName("wireless"),
                CoreMatchers.allOf(CoreMatchers.instanceOf(InterfaceType.class),
                        FeatureIs.featureIsEqualTo("name", "name", InterfaceType::getName, "wireless")));
        Assert.assertThat(interfaceTypeDao.getByName("virtual"),
                CoreMatchers.allOf(CoreMatchers.instanceOf(InterfaceType.class),
                        FeatureIs.featureIsEqualTo("name", "name", InterfaceType::getName, "virtual")));
    }
}