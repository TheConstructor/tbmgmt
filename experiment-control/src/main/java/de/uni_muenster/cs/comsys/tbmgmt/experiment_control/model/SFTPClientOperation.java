package de.uni_muenster.cs.comsys.tbmgmt.experiment_control.model;

import net.schmizz.sshj.sftp.SFTPClient;

import java.io.IOException;

/**
 * Created by matthias on 13.11.15.
 */
@FunctionalInterface
public interface SFTPClientOperation<Result> {

    Result execute(SFTPClient sftpClient) throws IOException, InterruptedException;
}
