package org.nineml.logging;

import java.util.HashMap;

/**
 * The abstract class that all concrete loggers must extend.
 */
public abstract class Logger {
    /**
     * The logging catagory for logger messages.
     */
    public static final String logcategory = "Logger";

    /**
     * The system property for setting the default log level.
     */
    public static final String defaultLogLevelProperty = "org.nineml.logging.defaultLogLevel";

    /**
     * The system property for setting the category:loglevel mappings.
     */
    public static final String logLevelsProperty = "org.nineml.logging.logLevels";

    /**
     * The log level to indicate no logging, not even errors.
     */
    public static final int SILENT = 0;
    /**
     * The log level for error messages.
     */
    public static final int ERROR = 1;
    /**
     * The log level for warning messages.
     */
    public static final int WARNING = 2;
    /**
     * The log level for informational messages.
     */
    public static final int INFO = 3;
    /**
     * The log level for debug messages.
     */
    public static final int DEBUG = 4;
    /**
     * The log level for trace messages.
     */
    public static final int TRACE = 5;

    private static final HashMap<String,Integer> levelNames = new HashMap<>();
    static {
        levelNames.put("silent", SILENT);
        levelNames.put("error", ERROR);
        levelNames.put("warning", WARNING);
        levelNames.put("info", INFO);
        levelNames.put("debug", DEBUG);
        levelNames.put("trace", TRACE);
    }

    private int defaultLogLevel = ERROR;

    private static final HashMap<String,Integer> logLevels = new HashMap<>();

    /**
     * Set the log levels by reading system properties.
     * <p>The properties are:</p>
     * <ul>
     *     <li>{@link #defaultLogLevelProperty}
     *     <p>Must be an integer specifying the initial default log level or one of the strings
     *     "silent", "error", "warning", "info", "debug", or "trace". Absent this property,
     *     the default level is 5, or "error".</p>
     *     </li>
     *     <li>{@link #logLevelsProperty}
     *     <p>This property specifies a mapping between log categories and the log level for that category.
     *     The format of the property is a list of comma or space separated values of the form
     *     "category:level".</p>
     *     </li>
     * </ul>
     */
    public void readSystemProperties() {
        String value = System.getProperty(defaultLogLevelProperty);
        if (value != null) {
            defaultLogLevel = logLevelNumber(value);
        }

        value = System.getProperty(logLevelsProperty);
        if (value != null) {
            for (String pair : value.split("[,\\s]+")) {
                if (pair.contains(":")) {
                    int pos = pair.indexOf(":");
                    String name = pair.substring(0, pos);
                    value = pair.substring(pos+1);
                    logLevels.put(name, logLevelNumber(value));
                } else {
                    error(logcategory, "Cannot parse log level setting: %s", pair);
                }
            }
        }
    }

    private int logLevelNumber(String name) {
        if (Character.isDigit(name.charAt(0))) {
            try {
                int value = Integer.parseInt(name);
                return Math.max(0, value);
            } catch (NumberFormatException ex) {
                error(logcategory, "Failed to parse log level: %s", name);
                return ERROR;
            }
        }

        if (levelNames.containsKey(name)) {
            return levelNames.get(name);
        }

        error(logcategory, "Unknown log level specified: %s", name);
        return ERROR;
    }

    /**
     * Get the default log level
     * @return the default log level
     */
    public int getDefaultLogLevel() {
        return defaultLogLevel;
    }

    /**
     * Set the default log level.
     * @param level the level
     */
    public void setDefaultLogLevel(int level) {
        defaultLogLevel = Math.max(0, level);
    }

    /**
     * Set the default log level.
     * <p>The level must be "silent", "error", "warning", "info", "debug", or "trace". If an invalid
     * value is specified, "error" is used.</p>
     * @param level the level.
     */
    public void setDefaultLogLevel(String level) {
        setDefaultLogLevel(logLevelNumber(level));
    }

    /**
     * Get the log level for a particular category.
     * @param category the category
     * @return the level
     */
    public int getLogLevel(String category) {
        return logLevels.getOrDefault(category, defaultLogLevel);
    }

    /**
     * Set the log level for a particular category.
     * @param category the category
     * @param level the level
     */
    public void setLogLevel(String category, int level) {
        logLevels.put(category, Math.max(0, level));
    }

    /**
     * Set the log level for a particular category.
     * <p>The level must be "silent", "error", "warning", "info", "debug", or "trace". If an invalid
     * value is specified, "error" is used.</p>
     * @param level the level.
     */
    public void setLogLevel(String category, String level) {
        logLevels.put(category, logLevelNumber(level));
    }

    protected String message(String category, int level, String message, Object... params) {
        StringBuilder sb = new StringBuilder();
        switch (level) {
            case ERROR:
                sb.append("ERROR: ");
                break;
            case WARNING:
                sb.append("WARN: ");
                break;
            case INFO:
                sb.append("INFO: ");
                break;
            default:
                sb.append("DEBUG: ");
                break;
        }

        sb.append(String.format(message, params));
        return sb.toString();
    }

    /** Issue an error message. */
    public abstract void error(String category, String message, Object... params);
    /** Issue a warning message. */
    public abstract void warn(String category, String message, Object... params);
    /** Issue an informational message. */
    public abstract void info(String category, String message, Object... params);
    /** Issue a debug message. */
    public abstract void debug(String category, String message, Object... params);
    /** Issue a trace message. */
    public abstract void trace(String category, String message, Object... params);
}
