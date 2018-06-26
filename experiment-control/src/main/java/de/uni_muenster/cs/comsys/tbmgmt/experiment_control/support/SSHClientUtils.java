package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;
import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.NodeInterface;
import de.uni_muenster.cs.comsys.tbmgmt.core.model.LogEntryCreator;
import de.uni_muenster.cs.comsys.tbmgmt.core.utils.ImpatientBufferedReader;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by matthias on 11.12.15.
 */
public final class SSHClientUtils {

    private static final Logger LOG = Logger.getLogger(SSHClientUtils.class.getName());
    public static final Charset CONSOLE_CHARSET = StandardCharsets.UTF_8;
    public static final DefaultConfig SSH_CONFIG;

    static {
        SSH_CONFIG = new DefaultConfig();
        // Keep-Alive every 30 seconds
        SSH_CONFIG.setKeepAliveProvider(new SSHKeepAliveProvider(30));
        // Hack to prevent OpenSSH from sending "hostkeys-00@openssh.com"-message
        SSH_CONFIG.setVersion("TTSSH/2.72 - " + SSH_CONFIG.getVersion());
    }

    public static SSHClient clientFor(final Node node, final ResourcePatternResolver resourcePatternResolver,
                                      final String username)
            throws IOException {
        final SSHClient sshClient = new SSHClient(SSH_CONFIG);
        try {
            sshClient.loadKnownHosts();
        } catch (final IOException e) {
            LOG.log(Level.FINE, "Could not load known_hosts", e);
        }
        sshClient.addHostKeyVerifier((hostname, port, key) -> {
            LOG.log(Level.FINE,
                    "Could not verify authenticity of " + hostname + " with " + key.getAlgorithm() + " key " + key);
            return true;
        });

        final List<IOException> exceptions = new ArrayList<>();
        for (final NodeInterface nodeInterface : node.getInterfaces()) {
            if (nodeInterface.isControlledOverThisConnection()) {
                if (nodeInterface.getIpv6Address() != null) {
                    try {
                        sshClient.connect(nodeInterface.getIpv6Address());
                        break;
                    } catch (final IOException e) {
                        exceptions.add(e);
                    }
                }
                if (nodeInterface.getIpv4Address() != null) {
                    try {
                        sshClient.connect(nodeInterface.getIpv4Address());
                        break;
                    } catch (final IOException e) {
                        exceptions.add(e);
                    }
                }
            }
        }
        if (!sshClient.isConnected()) {
            final String message =
                    String.format("No useable control-interface found for node with name %s", node.getName());
            if (exceptions.isEmpty()) {
                // no exceptions -> we did not even try to connect ._.
                throw new IllegalStateException(message);
            } else {
                final IOException ioException = new IOException(message, exceptions.remove(0));
                for (final IOException exception : exceptions) {
                    ioException.addSuppressed(exception);
                }
                throw ioException;
            }
        }

        boolean authSucceeded = false;
        try {
            final ArrayList<KeyProvider> keyProviders = new ArrayList<>();

            tryLoadKey(sshClient, keyProviders, resourcePatternResolver,
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "default_key.dsa");
            tryLoadKey(sshClient, keyProviders, resourcePatternResolver,
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "default_key.rsa");

            final String base = System.getProperty("user.home") + File.separator + ".ssh" + File.separator;
            tryLoadKey(sshClient, keyProviders, base + "id_rsa");
            tryLoadKey(sshClient, keyProviders, base + "id_dsa");

            sshClient.authPublickey(username, keyProviders);
            authSucceeded = true;
            return sshClient;
        } finally {
            if (!authSucceeded) {
                sshClient.close();
            }
        }
    }

    private static void tryLoadKey(final SSHClient sshClient, final ArrayList<KeyProvider> keyProviders,
                                   final ResourcePatternResolver resourcePatternResolver,
                                   final String locationPattern) {
        try {
            final Resource[] resources = resourcePatternResolver.getResources(locationPattern);
            for (final Resource resource : resources) {
                tryLoadKey(sshClient, keyProviders, resource);
            }
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e, () -> "Could not load " + locationPattern + " as private SSH-key");
        }
    }

    private static void tryLoadKey(final SSHClient sshClient, final ArrayList<KeyProvider> keyProviders,
                                   final Resource resource) {
        try {
            keyProviders.add(sshClient.loadKeys(resource.getFile().getAbsolutePath()));
        } catch (final IOException e1) {
            // We probably just tried to load a resource from a jar and getFile() blew
            try {
                final PKCS8KeyFile keyFile = new PKCS8KeyFile();
                keyFile.init(new InputStreamReader(resource.getInputStream()));
                keyProviders.add(keyFile);
            } catch (final IOException e) {
                e.addSuppressed(e1);
                LOG.log(Level.WARNING, "Could not load " + resource + " as private SSH-key", e);
            }
        }
    }

    private static void tryLoadKey(final SSHClient sshClient, final ArrayList<KeyProvider> keyProviders,
                                   final String filename) {
        if (!new File(filename).exists()) {
            return;
        }
        try {
            keyProviders.add(sshClient.loadKeys(filename));
        } catch (final IOException e) {
            LOG.log(Level.WARNING, e, () -> "Could not load " + filename + " as private SSH-key");
        }
    }

    public static Integer executeCommand(final Session session, final String commandline,
                                         final Duration commandDuration, final LogEntryCreator logEntryCreator)
            throws IOException {
        return executeCommand(session, commandline, commandDuration, logEntryCreator, logEntryCreator);
    }

    public static Integer executeCommand(final Session session, final String commandline,
                                         final Duration commandDuration, final LogEntryCreator logEntryCreator,
                                         final AtomicBoolean terminated) throws IOException {
        return executeCommand(session, commandline, commandDuration, logEntryCreator, logEntryCreator, terminated);
    }

    public static Integer executeCommand(final Session session, final String commandline,
                                         final Duration commandDuration, final LogEntryCreator logEntryCreator,
                                         final LogEntryCreator stdOutLogEntryCreator) throws IOException {
        return executeCommand(session, commandline, commandDuration, logEntryCreator, stdOutLogEntryCreator,
                new AtomicBoolean(false));
    }

    /**
     * @param session               Session to execute the command in. This will be the only action applied to the
     *                              action.
     * @param commandline           the commandline to execute
     * @param commandDuration       the time to wait for termination or {@code null} to just see how long it will take
     * @param logEntryCreator       general purpose
     *                              {@link LogEntryCreator}. StdOut will be logged with {@link LogEntryCreator.LogReason#STDOUT} here
     * @param stdOutLogEntryCreator StdErr will be logged with {@link LogEntryCreator.LogReason#STDERR} here
     * @param terminated            should initially contain {@code false} and can be used to terminate the command
     *                              without interrupting the Thread
     */
    public static Integer executeCommand(final Session session, final String commandline,
                                         final Duration commandDuration, final LogEntryCreator logEntryCreator,
                                         final LogEntryCreator stdOutLogEntryCreator, final AtomicBoolean terminated)
            throws IOException {
        // While it is strictly not required to allocate a tty it speeds up process termination on connection loss
        // by sending SIG_HUP to the executed command
        session.allocateDefaultPTY();

        if (terminated.get()) {
            return null;
        }
        try (Session.Command command = session.exec(commandline)) {
            final Instant startOfCommand = Instant.now();
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO, "Started command: " + commandline);

            // command.isOpen() seems to indicate a running command
            try (ImpatientBufferedReader stdout = new ImpatientBufferedReader(
                    new InputStreamReader(command.getInputStream(), CONSOLE_CHARSET));
                 ImpatientBufferedReader stderr = new ImpatientBufferedReader(
                         new InputStreamReader(command.getErrorStream(), CONSOLE_CHARSET))) {
                try {
                    if (commandDuration != null && !commandDuration.isNegative() && !commandDuration.isZero()) {
                        final Instant terminationPoint = startOfCommand.plus(commandDuration);
                        outer:
                        while (command.isOpen()) {
                            temporarilyJoinCommand(command);
                            do {
                                tryReadToLog(stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                                if (terminated.get()) {
                                    logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                                            "Terminating command after termination was requested: " + commandline);
                                    terminateCommand(command, stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                                    break outer;
                                }
                                if (!terminationPoint.isAfter(Instant.now())) {
                                    logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                                            "Terminating command after exceeding designated run-duration: "
                                                    + commandline);
                                    terminateCommand(command, stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                                    break outer;
                                }
                            } while ((stderr.ready() || stdout.ready()) && command.isOpen());
                        }
                    } else {
                        outer:
                        while (command.isOpen()) {
                            temporarilyJoinCommand(command);
                            do {
                                tryReadToLog(stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                                if (terminated.get()) {
                                    logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                                            "Terminating command after termination was requested: " + commandline);
                                    terminateCommand(command, stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                                    break outer;
                                }
                            } while ((stderr.ready() || stdout.ready()) && command.isOpen());
                        }
                    }
                } catch (final ConnectionException e) {
                    if (e.getCause() instanceof InterruptedException
                            || e.getCause() instanceof InterruptedIOException) {
                        logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                                "Terminating command after receiving interrupt: " + commandline);
                        terminateCommand(command, stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                        readStreamsToEnd(stdout, stdOutLogEntryCreator, stderr, logEntryCreator);
                    }
                    throw e;
                } catch (final InterruptedIOException e) {
                    logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO,
                            "Terminating command after receiving interrupt: " + commandline);
                    terminateCommand(command, stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
                    readStreamsToEnd(stdout, stdOutLogEntryCreator, stderr, logEntryCreator);
                    throw e;
                }
                readStreamsToEnd(stdout, stdOutLogEntryCreator, stderr, logEntryCreator);
                return command.getExitStatus();
            }
        } catch (final Exception e) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            printWriter.flush();
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.ERROR,
                    "Caught exception: " + stringWriter.toString());
            throw e;
        }
    }

    private static void terminateCommand(final Session.Command command, final LogEntryCreator stdOutLogEntryCreator,
                                         final LogEntryCreator logEntryCreator, final ImpatientBufferedReader stdout,
                                         final ImpatientBufferedReader stderr) throws IOException {
        if (!command.isOpen()) {
            return;
        }
        try {
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO, "Sending command INT-signal");

            // per http://stackoverflow.com/a/19985859/1266906 writing Ctrl+C is supported by OpenSSH while receiving
            // signals is not. We do both. Also have a look at ExperimentControlConfiguration.isUseCoreutilsTimeout
            // for yet another way to terminate time-limited commands without needing support by the ssh server

            // Send Ctrl+C (character code is 0x03):
            command.getOutputStream().write(3);
            command.getOutputStream().flush();

            command.signal(Signal.INT);
        } catch (final TransportException e) {
            LOG.log(Level.INFO, "Could not send INT", e);
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO, "Could not send INT");
            return;
        }
        final Instant killTime = Instant.now().plusSeconds(10);
        do {
            if (temporarilyJoinCommand(command)) {
                return;
            }
            tryReadToLog(stdOutLogEntryCreator, logEntryCreator, stdout, stderr);
        } while (command.isOpen() && Instant.now().isBefore(killTime));
        if (command.isOpen()) {
            try {
                logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO, "Sending command KILL-signal");
                command.signal(Signal.KILL);
                command.close();
            } catch (final TransportException e) {
                LOG.log(Level.INFO, "Could not send KILL", e);
                logEntryCreator.createLogEntry(LogEntryCreator.LogReason.INFO, "Could not send KILL");
            }
        }
    }

    private static void readStreamsToEnd(final ImpatientBufferedReader stdout,
                                         final LogEntryCreator stdOutLogEntryCreator,
                                         final ImpatientBufferedReader stderr, final LogEntryCreator logEntryCreator)
            throws IOException {
        while (!stderr.hasReachedEOF()) {
            final String line = stderr.readLine(true);
            logStdErr(logEntryCreator, line);
        }
        while (!stdout.hasReachedEOF()) {
            final String line = stdout.readLine(true);
            logStdOut(stdOutLogEntryCreator, line);
        }
    }

    private static boolean temporarilyJoinCommand(final Session.Command command) throws ConnectionException {
        try {
            command.join(100, TimeUnit.MILLISECONDS);
            return true;
        } catch (final ConnectionException e) {
            // If the time passes and the command is still running we receive a TimeoutException <.<
            if (!(e.getCause() instanceof TimeoutException)) {
                throw e;
            }
        }
        return false;
    }

    private static void tryReadToLog(final LogEntryCreator stdOutLogEntryCreator, final LogEntryCreator logEntryCreator,
                                     final ImpatientBufferedReader stdout, final ImpatientBufferedReader stderr)
            throws IOException {
        if (stderr.ready()) {
            final String line = stderr.readLine();
            logStdErr(logEntryCreator, line);
        }
        if (stdout.ready()) {
            final String line = stdout.readLine();
            logStdOut(stdOutLogEntryCreator, line);
        }
    }

    private static void logStdOut(final LogEntryCreator logEntryCreator, final String line) {
        if (line != null) {
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.STDOUT, line);
        }
    }

    private static void logStdErr(final LogEntryCreator logEntryCreator, final String line) {
        if (line != null) {
            logEntryCreator.createLogEntry(LogEntryCreator.LogReason.STDERR, line);
        }
    }
}
