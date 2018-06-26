package de.uni_muenster.cs.comsys.tbmgmt.core.model;

import java.util.logging.Level;

/**
 * Created by matthias on 09.01.16.
 */
public enum LogLevel {
    DEBUG, INFO, WARN, ERROR;

    public static LogLevel from(final Level level) {
        if (level == null) {
            return null;
        }
        final int intValue = level.intValue();
        if (intValue >= Level.SEVERE.intValue()) {
            return ERROR;
        }
        if (intValue >= Level.WARNING.intValue()) {
            return WARN;
        }
        if (intValue >= Level.INFO.intValue()) {
            return INFO;
        }
        return DEBUG;
    }
}
