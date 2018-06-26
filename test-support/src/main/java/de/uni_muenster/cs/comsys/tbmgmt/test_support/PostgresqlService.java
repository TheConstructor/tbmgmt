package de.uni_muenster.cs.comsys.tbmgmt.test_support;

import de.flapdoodle.embed.process.io.directories.FixedPath;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.qatools.embed.postgresql.Command;
import ru.yandex.qatools.embed.postgresql.PostgresExecutable;
import ru.yandex.qatools.embed.postgresql.PostgresProcess;
import ru.yandex.qatools.embed.postgresql.PostgresStarter;
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.DownloadConfigBuilder;
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig;
import ru.yandex.qatools.embed.postgresql.config.RuntimeConfigBuilder;
import ru.yandex.qatools.embed.postgresql.distribution.Version;
import ru.yandex.qatools.embed.postgresql.ext.ArtifactStoreBuilder;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Based upon https://github.com/yandex-qatools/postgresql-embedded/blob/master/src/test/java/ru/yandex/qatools/embed
 * /postgresql/PostgresqlService.java
 */
public class PostgresqlService implements InitializingBean, DisposableBean {

    @Value("${tbmgmt.test.postgresPath}")
    private String postgresPath;
    private PostgresProcess process;
    private PostgresConfig config;

    @Override
    public void afterPropertiesSet() throws Exception {
        final PostgresStarter<PostgresExecutable, PostgresProcess> runtime = PostgresStarter.getInstance(
                new RuntimeConfigBuilder()
                        .defaults(Command.Postgres)
                        .artifactStore(new ArtifactStoreBuilder()
                                .defaults(Command.Postgres)
                                .tempDir(new FixedPath(postgresPath + "/tmp"))
                                .download(new DownloadConfigBuilder()
                                        .defaultsForCommand(Command.Postgres)
                                        .artifactStorePath(new FixedPath(postgresPath + "/artifacts"))
                                        .build()))
                        .build());
        config =
                new PostgresConfig(Version.Main.PRODUCTION, new AbstractPostgresConfig.Net("localhost", findFreePort()),
                        new AbstractPostgresConfig.Storage("test", postgresPath + "/storage"),
                        new AbstractPostgresConfig.Timeout(),
                        new AbstractPostgresConfig.Credentials("user", "password"));
        final PostgresExecutable exec = runtime.prepare(config);
        process = exec.start();
    }

    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:%s/%s", config.net().host(), config.net().port(),
                config.storage().dbName());
    }

    public PostgresProcess getProcess() {
        return process;
    }

    public PostgresConfig getConfig() {
        return config;
    }

    @Override
    public void destroy() throws Exception {
        process.stop();
    }

    /**
     * Returns a free port number on localhost.
     * <p/>
     * Based upon https://github.com/yandex-qatools/postgresql-embedded/blob/master/src/test/java/ru/yandex/qatools
     * /embed/postgresql/util/SocketUtil.java
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    public static int findFreePort() {
        try {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Could not find a free TCP/IP port to start embedded PostgreSQL server on",
                    e);
        }
    }
}