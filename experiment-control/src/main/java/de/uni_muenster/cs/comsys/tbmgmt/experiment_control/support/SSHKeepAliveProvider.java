package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.support;

import net.schmizz.keepalive.KeepAlive;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.connection.ConnectionImpl;

/**
 * Created by matthias on 13.11.15.
 */
public class SSHKeepAliveProvider extends KeepAliveProvider {

    private final int keepAliveInterval;

    public SSHKeepAliveProvider(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    @Override
    public KeepAlive provide(ConnectionImpl connection) {
        KeepAlive keepAlive = KeepAliveProvider.KEEP_ALIVE.provide(connection);
        keepAlive.setKeepAliveInterval(keepAliveInterval);
        return keepAlive;
    }
}
