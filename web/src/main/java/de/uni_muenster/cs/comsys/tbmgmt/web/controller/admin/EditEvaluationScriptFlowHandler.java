package de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.EvaluationScriptDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.EvaluationScript;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by matthias on 04.04.15.
 */
public class EditEvaluationScriptFlowHandler extends AbstractFlowHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EditEvaluationScriptFlowHandler.class);

    @Autowired
    private EvaluationScriptDao evaluationScriptDao;

    @Override
    public String getFlowId() {
        return "admin/editEvaluationScript";
    }

    @Override
    public MutableAttributeMap<Object> createExecutionInputMap(final HttpServletRequest request) {
        return super.createExecutionInputMap(request);
    }

    @Override
    @Transactional
    public String handleExecutionOutcome(final FlowExecutionOutcome outcome, final HttpServletRequest request,
                                         final HttpServletResponse response) {
        return "/admin/evaluationScripts";
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
    public EvaluationScript provideEntity(final String id) {
        if (!StringUtils.isBlank(id)) {
            try {
                final EvaluationScript evaluationScript = evaluationScriptDao.find(Long.parseLong(id));
                if (evaluationScript != null) {
                    return evaluationScript;
                } else {
                    LOG.info(String.format("Received unknown evaluationScript-ID \"%s\"", id));
                }
            } catch (final NumberFormatException e) {
                LOG.info("Received invalid evaluationScript-ID", e);
            }
        }
        return new EvaluationScript();
    }
}
