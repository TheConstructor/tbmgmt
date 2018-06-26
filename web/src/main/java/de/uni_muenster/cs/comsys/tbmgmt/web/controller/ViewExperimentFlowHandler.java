package de.uni_muenster.cs.comsys.tbmgmt.web.controller;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.experiment.ExperimentDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.web.support.TbmgmtWebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.model.builder.FlowModelBuilderException;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by matthias on 04.04.15.
 */
public class ViewExperimentFlowHandler extends AbstractFlowHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ViewExperimentFlowHandler.class);

    @Autowired
    private ExperimentDao experimentDao;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public String getFlowId() {
        return "viewExperiment";
    }

    @Override
    public MutableAttributeMap<Object> createExecutionInputMap(final HttpServletRequest request) {
        return super.createExecutionInputMap(request);
    }

    @Override
    @Transactional
    public String handleExecutionOutcome(final FlowExecutionOutcome outcome, final HttpServletRequest request,
                                         final HttpServletResponse response) {

        if ("store".equals(outcome.getId())) {
            // experimentDao.merge(outcome.getOutput().get("experiment", Experiment.class));
        }
        return "/experiments/";
    }

    @Override
    public String handleException(final FlowException e, final HttpServletRequest request,
                                  final HttpServletResponse response) {
        if (e instanceof NoSuchFlowExecutionException) {
            return null;
        } else {
            throw e;
        }
    }

    /**
     * Separate method to be called by flow-expression so we are in the right persistence-context and lazy loading, ...
     * works
     */
    public Experiment provideEntity(final String id) {
        final Authentication authentication = TbmgmtWebUtils.getAuthenticatedAuthentication(authenticationManager);
        TbmgmtWebUtils.isFullyAuthenticated(authentication, null);
        if (!StringUtils.isBlank(id)) {
            try {
                final Experiment experiment = experimentDao.find(Long.parseLong(id));
                if (experiment != null) {
                    return experiment;
                } else {
                    LOG.info(String.format("Received unknown experiment-ID \"%s\"", id));
                }
            } catch (final NumberFormatException e) {
                LOG.info("Received invalid experiment-ID", e);
            }
        }
        throw new FlowModelBuilderException("Can not provide experiment with id " + id);
    }
}
