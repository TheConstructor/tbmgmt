package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.db.entities.node.Node;

import java.io.IOException;

/**
 * Created by matthias on 28.02.16.
 */
@FunctionalInterface
public interface SSHSessionOperationExecutor {
    <R> R executeOn(Node node, String username, SSHSessionOperation<R> operation)
            throws IOException, InterruptedException;
}
