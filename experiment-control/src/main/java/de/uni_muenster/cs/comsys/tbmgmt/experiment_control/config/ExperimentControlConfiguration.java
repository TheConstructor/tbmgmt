package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.config;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.Experiment;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.action.ExperimentAction;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentActionExecution;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.experiment.result.ExperimentReplicationVariableValues;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * Created by matthias on 31.03.15.
 */
@ConfigurationProperties(prefix = "tbmgmt.experiment.control", ignoreUnknownFields = false)
public class ExperimentControlConfiguration {

    private static final Log LOG = LogFactory.getLog(ExperimentControlConfiguration.class.getName());

    /**
     * How long to wait before re-checking for other experiments operating on the same nodes.
     */
    private int secondsBetweenNodeAvailabilityChecks = 30;
    /**
     * How long to wait after rebooting nodes before performing the first alive-check.
     */
    private int secondsToWaitForNodeToReboot = 90;
    /**
     * Seconds between consecutive alive-checks.
     */
    private int secondsBetweenAliveChecks = 10;
    private int aliveCheckRetries = 6;
    private String rebootCommand = "shutdown -r +0";
    private String commandPrefix = "";
    private String evaluationScriptRunner = "/usr/bin/env python";
    /**
     * Files will be uploaded to nodeDirectory make sure it contains experiment and node
     */
    private String nodeDirectory = "#experiment.id + '/' + #node.id + '/'";
    private Expression nodeDirectoryExpression;
    /**
     * experimentDirectory will be deleted post-experiment
     */
    private String experimentDirectory = "#experiment.id + '/'";
    private Expression experimentDirectoryExpression;
    /**
     * replicationDirectory is used as the working-path to run actions
     */
    private String replicationDirectory = "#experiment.id + '/' + #node.id + '/' + #replication +'/'";
    private Expression replicationDirectoryExpression;
    /**
     * This is used when referring to experiment's files, which reside in nodeDirectory when running things in
     * replicationDirectory
     */
    private String replicationToNodeDirectory = "'../'";
    private Expression replicationToNodeDirectoryExpression;
    /**
     * File name to store command output inside the replicationDirectory
     */
    private String commandOutputFileName = "#action.id + '-' + #actionExecution.id + '.stdout'";
    private Expression commandOutputFileNameExpression;

    private File bootConfigurationDirectory = new File("/tmp/tbmgmt/tftpboot/pxelinux.cfg");
    private File hostsFile = new File("/tmp/tbmgmt/hosts");
    private File dhcpHostsFile = new File("/tmp/tbmgmt/dhcp-hosts");
    private String automationUsername = "testbed-user";
    private int maxParallelExperimentExecutions = 10;
    private boolean useCoreutilsTimeout = true;
    private boolean reuseSSHConnections = true;
    /**
     * To keep the number of open JDBC-Connections low and as Hibernate is managing EntityManagers and therefore
     * Connections on a per-thread basis, we use a thread-pool to store Log-Messages and any result of
     * action-execution. Number of needed connections should be actionResultStoragePoolSize +
     * maxParallelExperimentExecutions + 2.
     */
    private int actionResultStoragePoolSize = 10;

    @PostConstruct
    public void init() {
        LOG.debug("Using Experiment-Control-Configuration: " + this);
        final SpelParserConfiguration spelParserConfiguration =
                new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, getClass().getClassLoader());
        final SpelExpressionParser spelExpressionParser = new SpelExpressionParser(spelParserConfiguration);
        nodeDirectoryExpression = spelExpressionParser.parseExpression(nodeDirectory);
        experimentDirectoryExpression = spelExpressionParser.parseExpression(experimentDirectory);
        replicationDirectoryExpression = spelExpressionParser.parseExpression(replicationDirectory);
        replicationToNodeDirectoryExpression = spelExpressionParser.parseExpression(replicationToNodeDirectory);
        commandOutputFileNameExpression = spelExpressionParser.parseExpression(commandOutputFileName);
    }

    public int getSecondsBetweenNodeAvailabilityChecks() {
        return secondsBetweenNodeAvailabilityChecks;
    }

    public void setSecondsBetweenNodeAvailabilityChecks(final int secondsBetweenNodeAvailabilityChecks) {
        this.secondsBetweenNodeAvailabilityChecks = secondsBetweenNodeAvailabilityChecks;
    }

    public int getSecondsToWaitForNodeToReboot() {
        return secondsToWaitForNodeToReboot;
    }

    public void setSecondsToWaitForNodeToReboot(final int secondsToWaitForNodeToReboot) {
        this.secondsToWaitForNodeToReboot = secondsToWaitForNodeToReboot;
    }

    public int getSecondsBetweenAliveChecks() {
        return secondsBetweenAliveChecks;
    }

    public void setSecondsBetweenAliveChecks(final int secondsBetweenAliveChecks) {
        this.secondsBetweenAliveChecks = secondsBetweenAliveChecks;
    }

    public int getAliveCheckRetries() {
        return aliveCheckRetries;
    }

    public void setAliveCheckRetries(final int aliveCheckRetries) {
        this.aliveCheckRetries = aliveCheckRetries;
    }

    public String getRebootCommand() {
        return rebootCommand;
    }

    public void setRebootCommand(final String rebootCommand) {
        this.rebootCommand = rebootCommand;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(final String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public String getEvaluationScriptRunner() {
        return evaluationScriptRunner;
    }

    public void setEvaluationScriptRunner(final String evaluationScriptRunner) {
        this.evaluationScriptRunner = evaluationScriptRunner;
    }

    public String getNodeDirectory() {
        return nodeDirectory;
    }

    public void setNodeDirectory(final String nodeDirectory) {
        this.nodeDirectory = nodeDirectory;
    }

    public String getNodeDirectory(final Experiment experiment, final Node node) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("experiment", experiment);
        context.setVariable("node", node);
        return nodeDirectoryExpression.getValue(context, String.class);
    }

    public String getExperimentDirectory() {
        return experimentDirectory;
    }

    public void setExperimentDirectory(final String experimentDirectory) {
        this.experimentDirectory = experimentDirectory;
    }

    public String getExperimentDirectory(final Experiment experiment, final Node node) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("experiment", experiment);
        context.setVariable("node", node);
        return experimentDirectoryExpression.getValue(context, String.class);
    }

    public String getReplicationDirectory() {
        return replicationDirectory;
    }

    public void setReplicationDirectory(final String replicationDirectory) {
        this.replicationDirectory = replicationDirectory;
    }

    public String getReplicationDirectory(final Experiment experiment, final Node node, final long replication) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("experiment", experiment);
        context.setVariable("node", node);
        context.setVariable("replication", replication);
        return replicationDirectoryExpression.getValue(context, String.class);
    }

    public String getReplicationToNodeDirectory() {
        return replicationToNodeDirectory;
    }

    public void setReplicationToNodeDirectory(final String replicationToNodeDirectory) {
        this.replicationToNodeDirectory = replicationToNodeDirectory;
    }

    public String getReplicationToNodeDirectory(final Experiment experiment, final Node node) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("experiment", experiment);
        context.setVariable("node", node);
        return replicationToNodeDirectoryExpression.getValue(context, String.class);
    }

    public String getCommandOutputFileName() {
        return commandOutputFileName;
    }

    public void setCommandOutputFileName(final String commandOutputFileName) {
        this.commandOutputFileName = commandOutputFileName;
    }

    public String getCommandOutputFileName(final ExperimentAction action, final long replication,
                                           final ExperimentReplicationVariableValues variableValues,
                                           final ExperimentActionExecution actionExecution) {
        final StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("action", action);
        context.setVariable("replication", replication);
        context.setVariable("variableValues", variableValues);
        context.setVariable("actionExecution", actionExecution);
        return commandOutputFileNameExpression.getValue(context, String.class);
    }

    public File getBootConfigurationDirectory() {
        return bootConfigurationDirectory;
    }

    public void setBootConfigurationDirectory(final File bootConfigurationDirectory) {
        this.bootConfigurationDirectory = bootConfigurationDirectory;
    }

    public File getHostsFile() {
        return hostsFile;
    }

    public void setHostsFile(final File hostsFile) {
        this.hostsFile = hostsFile;
    }

    public File getDhcpHostsFile() {
        return dhcpHostsFile;
    }

    public void setDhcpHostsFile(final File dhcpHostsFile) {
        this.dhcpHostsFile = dhcpHostsFile;
    }

    public String getAutomationUsername() {
        return automationUsername;
    }

    public void setAutomationUsername(final String automationUsername) {
        this.automationUsername = automationUsername;
    }

    public int getMaxParallelExperimentExecutions() {
        return maxParallelExperimentExecutions;
    }

    public void setMaxParallelExperimentExecutions(final int maxParallelExperimentExecutions) {
        this.maxParallelExperimentExecutions = maxParallelExperimentExecutions;
    }

    public boolean isUseCoreutilsTimeout() {
        return useCoreutilsTimeout;
    }

    public void setUseCoreutilsTimeout(final boolean useCoreutilsTimeout) {
        this.useCoreutilsTimeout = useCoreutilsTimeout;
    }

    public boolean isReuseSSHConnections() {
        return reuseSSHConnections;
    }

    public void setReuseSSHConnections(final boolean reuseSSHConnections) {
        this.reuseSSHConnections = reuseSSHConnections;
    }

    public int getActionResultStoragePoolSize() {
        return actionResultStoragePoolSize;
    }

    public void setActionResultStoragePoolSize(final int actionResultStoragePoolSize) {
        this.actionResultStoragePoolSize = actionResultStoragePoolSize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("secondsBetweenNodeAvailabilityChecks", secondsBetweenNodeAvailabilityChecks)
                .append("secondsToWaitForNodeToReboot", secondsToWaitForNodeToReboot)
                .append("secondsBetweenAliveChecks", secondsBetweenAliveChecks)
                .append("aliveCheckRetries", aliveCheckRetries)
                .append("rebootCommand", rebootCommand)
                .append("commandPrefix", commandPrefix)
                .append("evaluationScriptRunner", evaluationScriptRunner)
                .append("nodeDirectory", nodeDirectory)
                .append("experimentDirectory", experimentDirectory)
                .append("replicationDirectory", replicationDirectory)
                .append("replicationToNodeDirectory", replicationToNodeDirectory)
                .append("commandOutputFileName", commandOutputFileName)
                .append("bootConfigurationDirectory", bootConfigurationDirectory)
                .append("hostsFile", hostsFile)
                .append("dhcpHostsFile", dhcpHostsFile)
                .append("automationUsername", automationUsername)
                .append("maxParallelExperimentExecutions", maxParallelExperimentExecutions)
                .append("useCoreutilsTimeout", useCoreutilsTimeout)
                .append("reuseSSHConnections", reuseSSHConnections)
                .append("actionResultStoragePoolSize", actionResultStoragePoolSize)
                .toString();
    }
}
