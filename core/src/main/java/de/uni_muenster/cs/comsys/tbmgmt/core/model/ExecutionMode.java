package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import de.uni_muenster.cs.comsys.tbmgmt.core.utils.EnumUtil;

import java.util.Map;

/**
 Created by matthias on 18.03.15.
 */
public enum ExecutionMode {
    /**
     server-mode: Runs as long as the whole experiment. No prior implementation found.
     */
    SERVER(0),
    /**
     Serial action execution. Starting with the first command all targeted nodes are instructed to execute the command,
     the executor waits for all nodes to complete the command and then executes the next command in the same fashion.
     */
    SERIAL(1),
    /**
     Parallel action execution. Starting with the first command all targeted nodes are instructed to execute the
     command,
     the executor immediately moves on to the next command and repeats the process.
     */
    PARALLEL(2),
    /**
     Time based action execution. Starting with the first command Threads are created for all targeted nodes, that will
     execute the command with the given timing, the executor immediately moves on to the next command and repeats the
     process.
     */
    TIMED(3);

    private static final Map<Integer, ExecutionMode> idValueMap = EnumUtil
            .getValueMap(ExecutionMode::getModeId, ExecutionMode.class);

    private final int modeId;

    ExecutionMode(final int modeId) {
        this.modeId = modeId;
    }

    public static ExecutionMode getByModeId(int modeId) {
        return idValueMap.get(modeId);
    }

    public int getModeId() {
        return modeId;
    }
}
