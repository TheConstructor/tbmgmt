package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.dao.node.NodeInterfaceDao;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.NodeConfigMonitor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config.ExperimentControlConfiguration;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.MustacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 17.02.16.
 */
public class NodeConfigMonitorImpl implements NodeConfigMonitor {

    private static final Logger LOG = Logger.getLogger(NodeConfigMonitorImpl.class.getName());

    private static final BinaryOperator<Instant> MAX_INSTANT_OPERATOR =
            BinaryOperator.maxBy(Comparator.nullsFirst(Comparator.naturalOrder()));

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private NodeInterfaceDao nodeInterfaceDao;

    @Autowired
    private TransactionTemplate readOnlyTransactionTemplate;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private ExperimentControlConfiguration experimentControlConfiguration;

    private Instant lastWrittenChange = null;


    @Override
    public synchronized void readNodeConfigFromDB() {
        try {
            readOnlyTransactionTemplate.execute(status -> {
                // If nothing changed (and we know it) we can skip evaluation of templates
                if (lastWrittenChange != null && nodeDao.entitiesUpdatedSince(lastWrittenChange) == 0
                        && nodeInterfaceDao.entitiesUpdatedSince(lastWrittenChange) == 0) {
                    return null;
                }

                final List<Node> nodes = nodeDao.getAllActiveNodesWithInterfaces();

                // Compute maximum of updated-columns first to ensure lazy-loading was triggered
                final Instant lastNodeUpdate =
                        nodes.stream().map(Node::getUpdated).reduce(MAX_INSTANT_OPERATOR).orElse(null);
                final Instant lastNodeInterfaceUpdate = nodes
                        .stream()
                        .flatMap(n -> n.getInterfaces().stream())
                        .map(NodeInterface::getUpdated)
                        .reduce(MAX_INSTANT_OPERATOR)
                        .orElse(null);

                // Load templates
                final MustacheResourceTemplateLoader templateLoader =
                        new MustacheResourceTemplateLoader("classpath:/templates/", ".mustache");
                templateLoader.setResourceLoader(resourcePatternResolver);
                final Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false).withLoader(templateLoader);
                final Template hostsTemplate = MustacheUtils.getTemplate(compiler, templateLoader, "hosts");
                final Template dhcpHostsfileTemplate =
                        MustacheUtils.getTemplate(compiler, templateLoader, "dhcp-hostsfile");

                // Render templates
                final boolean hostsChanged =
                        MustacheUtils.renderTemplateToPath(hostsTemplate, ImmutableMap.of("nodes", nodes),
                                experimentControlConfiguration.getHostsFile().toPath());
                final boolean dhcpHostsChanged =
                        MustacheUtils.renderTemplateToPath(dhcpHostsfileTemplate, ImmutableMap.of("nodes", nodes),
                                experimentControlConfiguration.getDhcpHostsFile().toPath());
                if (hostsChanged || dhcpHostsChanged) {
                    // TODO: reload dnsmasq
                    LOG.log(Level.INFO, "You need to reload dnsmasq-config");
                }

                // Only update lastWrittenChange after everything went smoothly.
                lastWrittenChange = MAX_INSTANT_OPERATOR.apply(lastNodeUpdate, lastNodeInterfaceUpdate);
                return null;
            });
        } catch (final TransactionException e) {
            LOG.log(Level.SEVERE, "Error updating dnsmasq-config", e);
        }
    }
}
