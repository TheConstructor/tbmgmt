package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by matthias on 22.11.15.
 */
public enum ExperimentState {
    SCHEDULED,
    WAITING_FOR_NODES,
    PREPARING,
    RUNNING,
    EVALUATING,
    CLEANING_UP,
    CANCELATION_REQUESTED,
    SUCCEEDED,
    FAILED;

    public static final List<ExperimentState> RUNNING_STATES;
    public static final List<ExperimentState> BLOCKING_STATES;
    public static final List<ExperimentState> END_STATES;

    static {
        final Set<ExperimentState> runningStates =
                Arrays.stream(ExperimentState.values()).filter(ExperimentState::isRunning).collect(Collectors.toSet());
        RUNNING_STATES = Collections.unmodifiableList(
                Arrays.asList(runningStates.toArray(new ExperimentState[runningStates.size()])));

        // Blocking states are all running states except for WAITING_FOR_NODES
        final Set<ExperimentState> blockingStates =
                runningStates.stream().filter(state -> !WAITING_FOR_NODES.equals(state)).collect(Collectors.toSet());
        BLOCKING_STATES = Collections.unmodifiableList(
                Arrays.asList(blockingStates.toArray(new ExperimentState[blockingStates.size()])));

        final Set<ExperimentState> endStates =
                Arrays.stream(ExperimentState.values()).filter(ExperimentState::isEndState).collect(Collectors.toSet());
        END_STATES =
                Collections.unmodifiableList(Arrays.asList(endStates.toArray(new ExperimentState[endStates.size()])));
    }

    public static List<ExperimentState> getRunningStates() {
        return RUNNING_STATES;
    }

    public static List<ExperimentState> getBlockingStates() {
        return BLOCKING_STATES;
    }

    public static List<ExperimentState> getEndStates() {
        return END_STATES;
    }

    public boolean allowsEdits() {
        switch (this) {
            case SCHEDULED:
                return true;
            case WAITING_FOR_NODES:
            case PREPARING:
            case RUNNING:
            case EVALUATING:
            case CLEANING_UP:
            case CANCELATION_REQUESTED:
            case SUCCEEDED:
            case FAILED:
                return false;
            default:
                throw new IllegalStateException("Unknown Enum-constant: " + this);
        }
    }

    public boolean isRunning() {
        switch (this) {
            case SCHEDULED:
                return false;
            case WAITING_FOR_NODES:
            case PREPARING:
            case RUNNING:
            case EVALUATING:
            case CLEANING_UP:
            case CANCELATION_REQUESTED:
                return true;
            case SUCCEEDED:
            case FAILED:
                return false;
            default:
                throw new IllegalStateException("Unknown Enum-constant: " + this);
        }
    }

    public boolean isEndState() {
        switch (this) {
            case SCHEDULED:
            case WAITING_FOR_NODES:
            case PREPARING:
            case RUNNING:
            case EVALUATING:
            case CLEANING_UP:
            case CANCELATION_REQUESTED:
                return false;
            case SUCCEEDED:
            case FAILED:
                return true;
            default:
                throw new IllegalStateException("Unknown Enum-constant: " + this);
        }
    }
}
