package de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.InterfaceTypeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.InterfaceType;
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
public class EditInterfaceTypeFlowHandler extends AbstractFlowHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EditInterfaceTypeFlowHandler.class);

    @Autowired
    private InterfaceTypeDao interfaceTypeDao;

    @Override
    public String getFlowId() {
        return "admin/editInterfaceType";
    }

    @Override
    public MutableAttributeMap<Object> createExecutionInputMap(final HttpServletRequest request) {
        return super.createExecutionInputMap(request);
    }

    @Override
    @Transactional
    public String handleExecutionOutcome(final FlowExecutionOutcome outcome, final HttpServletRequest request,
                                         final HttpServletResponse response) {
        return "/admin/interfaceTypes";
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
    public InterfaceType provideEntity(final String id) {
        if (!StringUtils.isBlank(id)) {
            try {
                final InterfaceType interfaceType = interfaceTypeDao.find(Long.parseLong(id));
                if (interfaceType != null) {
                    return interfaceType;
                } else {
                    LOG.info(String.format("Received unknown interfaceType-ID \"%s\"", id));
                }
            } catch (final NumberFormatException e) {
                LOG.info("Received invalid interfaceType-ID", e);
            }
        }
        return new InterfaceType();
    }
}
