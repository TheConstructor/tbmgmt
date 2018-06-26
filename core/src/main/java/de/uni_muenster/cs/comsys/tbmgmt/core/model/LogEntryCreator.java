package de.uni_muenster.cs.comsys.tbmgmt.core.model;

/**
 * Created by matthias on 10.01.16.
 */
@FunctionalInterface
public interface LogEntryCreator {
    void createLogEntry(LogReason logReason, String line);

    static LogLevel getLogLevel(final LogReason logReason) {
        switch (logReason) {
            default:
            case STDOUT:
                return LogLevel.INFO;
            case STDERR:
                return LogLevel.WARN;
            case DEBUG:
                return LogLevel.DEBUG;
            case INFO:
                return LogLevel.INFO;
            case WARNING:
                return LogLevel.WARN;
            case ERROR:
                return LogLevel.ERROR;
        }
    }

    static String prependMessage(final LogReason logReason, final String line) {
        switch (logReason) {
            case STDOUT:
                return "STDOUT: " + line;
            case STDERR:
                return "STDERR: " + line;
        }
        return line;
    }

    enum LogReason {
        STDOUT, STDERR, DEBUG, INFO, WARNING, ERROR
    }
}
