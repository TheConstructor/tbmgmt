package de.uni_muenster.cs.comsys.tbmgmt.web.controller.admin;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.core.FlowException;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.execution.FlowExecutionOutcome;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.mvc.servlet.AbstractFlowHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 Created by matthias on 04.04.15.
 */
public class EditNodeFlowHandler extends AbstractFlowHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EditNodeFlowHandler.class);

    @Autowired
    private NodeDao nodeDao;

    @Override
    public String getFlowId() {
        return "admin/editNode";
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
            // nodeDao.merge(outcome.getOutput().get("node", Node.class));
        }
        return "/admin/nodes";
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
     Separate method to be called by flow-expression so we are in the right persistence-context and lazy loading, ...
     works
     */
    public Node provideEntity(final String id) {
        if (!StringUtils.isBlank(id)) {
            try {
                Node node = nodeDao.find(Long.parseLong(id));
                if (node != null) {
                    return node;
                } else {
                    LOG.info(String.format("Received unknown node-ID \"%s\"", id));
                }
            } catch (NumberFormatException e) {
                LOG.info("Received invalid node-ID", e);
            }
        }
        return new Node();
    }

    public void applyChanges(final Node node, final ParameterMap parameters) {
        LOG.debug("Received parameters: " + parameters.toString());
        String applyValue = parameters.get("_eventId_adjust");
        if (applyValue != null) {
            String[] split = applyValue.split("-");
            switch (split[0]) {
                case "addI": {
                    NodeInterface nodeInterface = new NodeInterface();
                    nodeInterface.setNode(node);
                    node.getInterfaces().add(nodeInterface);
                    break;
                }
                case "delI": {
                    if (split.length < 2) {
                        break;
                    }
                    try {
                        int id = Integer.parseUnsignedInt(split[1]);
                        if (id >= 0 && id < node.getInterfaces().size()) {
                            node.getInterfaces().remove(id);
                        }
                    } catch (NumberFormatException e) {
                        LOG.info("Could not read number " + split[1], e);
                    }
                    break;
                }
            }
        }
    }
}
