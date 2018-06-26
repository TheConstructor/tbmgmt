package de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.impl;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.DbIntegrationTestSpringConfig;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.test_support.FeatureIs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by matthias on 18.02.16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DbIntegrationTestSpringConfig.class})
public class ExperimentDaoImplIT {

    @Autowired
    private ExperimentDao experimentDao;

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testMerge() throws Exception {
        experimentDao.merge(new Experiment());
    }

    @Test
    // @DirtiesContext would be correct, but deleting the merged experiment should do for now
    public void testMergeWithTransaction() throws Exception {
        final Experiment experiment = experimentDao.mergeWithTransaction(new Experiment());
        Assert.assertThat(experiment, FeatureIs.feature("id", "id", Experiment::getId, CoreMatchers.notNullValue()));
        // manual transaction -> manual clean-up
        experimentDao.getTransactionTemplate().execute(status -> {
            final Experiment experiment1 = experimentDao.find(experiment.getId());
            experimentDao.remove(experiment1);
            return null;
        });
    }

    @Test
    @Transactional
    public void testEntityCount() throws Exception {
        Assert.assertThat(experimentDao.entityCount(), CoreMatchers.is(0L));
        Assert.assertThat(experimentDao.merge(new Experiment()),
                FeatureIs.feature("id", "id", Experiment::getId, CoreMatchers.notNullValue()));
        Assert.assertThat(experimentDao.entityCount(), CoreMatchers.is(1L));
    }
}