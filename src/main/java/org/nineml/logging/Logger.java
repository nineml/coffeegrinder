package org.nineml.logging;

import java.util.HashMap;

public abstract class Logger {
    public static final int SILENT = 0;
    public static final int ERROR = 1;
    public static final int WARNING = 2;
    public static final int INFO = 3;
    public static final int DEBUG = 4;
    public static final int TRACE = 5;

    private int defaultLogLevel = ERROR;

    private static HashMap<String,Integer> logLevels = new HashMap<>();

    public int getDefaultLogLevel() {
        return defaultLogLevel;
    }

    public void setDefaultLogLevel(int level) {
        defaultLogLevel = Math.max(0, level);
    }

    public int getLogLevel(String category) {
        return logLevels.getOrDefault(category, defaultLogLevel);
    }

    public void setLogLevel(String category, int level) {
        logLevels.put(category, Math.max(0, level));
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

    public abstract void error(String category, String message, Object... params);
    public abstract void warn(String category, String message, Object... params);
    public abstract void info(String category, String message, Object... params);
    public abstract void debug(String category, String message, Object... params);
    public abstract void trace(String category, String message, Object... params);
}
