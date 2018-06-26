package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.executionSteps;

import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.MacAddress;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.TbmgmtUtil;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.impl.ExperimentExecutor;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model.ExperimentLogger;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.ActionResultStorageHelper;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.MustacheUtils;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.SSHClientUtils;
import de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support.TaskResultHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.mustache.MustacheResourceTemplateLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by matthias on 26.02.16.
 */
@Configurable
public class NodePreparator {

    private final ExperimentExecutor experimentExecutor;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private ActionResultStorageHelper actionResultStorageHelper;

    public NodePreparator(final ExperimentExecutor experimentExecutor) {
        this.experimentExecutor = experimentExecutor;
    }

    public List<Node> verifyNodeConfiguration(final Experiment experiment, final List<Node> nodes,
                                              final String username) {
        final List<Node> changedNodes = new ArrayList<>();
        final MustacheResourceTemplateLoader templateLoader =
                new MustacheResourceTemplateLoader("classpath:/templates/", ".mustache");
        templateLoader.setResourceLoader(resourcePatternResolver);
        final Mustache.Compiler compiler = Mustache.compiler().escapeHTML(false).withLoader(templateLoader);
        final Template nodeTemplate = MustacheUtils.getTemplate(compiler, templateLoader, "node-setup");
        final Path bootConfigurationDirectory = experimentExecutor
                .getExperimentControlConfiguration()
                .getBootConfigurationDirectory()
                .toPath()
                .normalize();
        try {
            TbmgmtUtil.ensureDirectoryExists(bootConfigurationDirectory);
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Could not verify presence of " + bootConfigurationDirectory + " or create it", e);
        }
        for (final Node node : nodes) {
            final Path targetPath = bootConfigurationDirectory.resolve(node.getName());
            if (MustacheUtils.renderTemplateToPath(nodeTemplate,
                    ImmutableMap.of("experiment", experiment, "node", node, "home", username), targetPath)) {
                changedNodes.add(node);
            }
            for (final NodeInterface nodeInterface : node.getInterfaces()) {
                if (nodeInterface.isControlledOverThisConnection()) {
                    final MacAddress macAddress = nodeInterface.getMacAddress();
                    if (macAddress != null) {
                        final Path macPath =
                                bootConfigurationDirectory.resolve("01-" + macAddress.getDashedString().toLowerCase());
                        if (!targetPath.equals(macPath)) {
                            TbmgmtUtil.linkOrCopy(macPath, targetPath);
                        }
                    }
                    final Inet4Address ipv4Address = nodeInterface.getIpv4Address();
                    if (ipv4Address != null) {
                        final Path ipv4Path = bootConfigurationDirectory.resolve(
                                String.format("%08X", TbmgmtUtil.addressAsInt(ipv4Address)));
                        if (!targetPath.equals(ipv4Path)) {
                            TbmgmtUtil.linkOrCopy(ipv4Path, targetPath);
                        }
                    }
                }
            }
        }
        return changedNodes;
    }

    public void restartNodes(final ExperimentLogger log, final Experiment experiment, final List<Node> nodes,
                             final ExecutorService executorService, final String username)
            throws ExecutionException, InterruptedException {
        final TaskResultHelper<Integer> taskResultHelper = new TaskResultHelper<>(executorService);
        for (final Node node : nodes) {
            taskResultHelper.submit(() -> {
                final Integer returnCode = experimentExecutor.executeOn(node, username,
                        session -> SSHClientUtils.executeCommand(session,
                                experimentExecutor.getExperimentControlConfiguration().getRebootCommand(), null,
                                actionResultStorageHelper.getLogEntryCreator(experiment, null, null, null, null,
                                        node)));
                if (returnCode != null && returnCode != 0) {
                    final String message = "Reboot command returned code " + returnCode + " on node " + node.getName();
                    log.log(Level.SEVERE, message);
                    throw new IllegalStateException(message);
                }
                return returnCode;
            });
        }
        taskResultHelper.collectOrThrowExceptions();

        experimentExecutor.closeSSHConnections(log);

        final int secondsToWaitForNodeToReboot =
                experimentExecutor.getExperimentControlConfiguration().getSecondsToWaitForNodeToReboot();
        log.log(Level.INFO, "Nodes asked to restart. Waiting " + secondsToWaitForNodeToReboot
                + " seconds before verifying reachabilty.");
        Thread.sleep(TimeUnit.SECONDS.toMillis(secondsToWaitForNodeToReboot));
    }

    public void checkNodesResponding(final ExperimentLogger log, final List<Node> nodes,
                                     final ExecutorService executorService, final String username)
            throws ExecutionException, InterruptedException {
        final TaskResultHelper<Boolean> taskResultHelper = new TaskResultHelper<>(executorService);
        for (final Node node : nodes) {
            taskResultHelper.submit(() -> {
                final int aliveCheckRetries =
                        experimentExecutor.getExperimentControlConfiguration().getAliveCheckRetries();
                for (int i = 0; i <= aliveCheckRetries; i++) {
                    try {
                        return experimentExecutor.executeOn(node, username, session -> true);
                    } catch (final IOException e) {
                        if (i < aliveCheckRetries) {
                            log.log(Level.FINE, "Could not connect to " + node.getName() + ". Retries left: " +
                                    (aliveCheckRetries - i), e);
                        } else {
                            throw e;
                        }
                    }
                    Thread.sleep(TimeUnit.SECONDS.toMillis(
                            experimentExecutor.getExperimentControlConfiguration().getSecondsBetweenAliveChecks()));
                }
                throw new IllegalStateException("Exceeded retries for node " + node.getName()
                        + " without receiving an IOException in the last try O.o");
            });
        }

        taskResultHelper.collectOrThrowExceptions();
    }
}
