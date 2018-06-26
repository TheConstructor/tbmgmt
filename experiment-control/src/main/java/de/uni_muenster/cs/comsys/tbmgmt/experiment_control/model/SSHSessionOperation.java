package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model;

import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;

/**
 * Created by matthias on 13.11.15.
 */
@FunctionalInterface
public interface SSHSessionOperation<Result> {

    Result execute(Session session) throws IOException, InterruptedException;
}
