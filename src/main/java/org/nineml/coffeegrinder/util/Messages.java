package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.ParseListener;

/**
 * Send messages to the {@link ParseListener} for this parse.
 *
 * <p>This is just weird sort of convenience wrapper so that I can have the "formatter"
 * versions of the message listener without cluttering up the API that users have to
 * implement if they want a custom listener.</p>
 */
public class Messages {
    private ParseListener listener;
    public Messages(ParseListener listener) {
        this.listener = listener;
    }

    // It's probably bad form to repeat the conditionals here, but let's not
    // interpolate the string if we're just going to discard it.

    /**
     * Format the parametesr as a debug message.
     * @param format The format string.
     * @param params The parameters.
     */
    public void debug(String format, Object... params) {
        if (listener.getMessageLevel() >= ParseListener.DEBUG) {
            listener.debug(String.format(format, params));
        }
    }

    /**
     * Format the parameters as a detail message.
     * @param format The format string.
     * @param params The parameters.
     */
    public void detail(String format, Object... params) {
        if (listener.getMessageLevel() >= ParseListener.DETAIL) {
            listener.detail(String.format(format, params));
        }
    }

    /**
     * Format the parameters as an info message.
     * @param format The format string.
     * @param params The parameters.
     */
    public void info(String format, Object... params) {
        if (listener.getMessageLevel() >= ParseListener.INFO) {
            listener.info(String.format(format, params));
        }
    }

    /**
     * Format the parameters as a warning message.
     * @param format The format string.
     * @param params The parameters.
     */
    public void warning(String format, Object... params) {
        if (listener.getMessageLevel() >= ParseListener.WARNING) {
            listener.warning(String.format(format, params));
        }
    }

    /**
     * Format the parameters as an error message.
     * @param format The format string.
     * @param params The parameters.
     */
    public void error(String format, Object... params) {
        if (listener.getMessageLevel() >= ParseListener.ERROR) {
            listener.error(String.format(format, params));
        }
    }

    /**
     * Emit a debug message.
     * <p>Tell me <em>everything</em> about the parse.</p>
     * @param message The message.
     */
    public void debug(String message) {
        listener.debug(message);
    }

    /**
     * Emit a detail message.
     * <p>Tell me about the details of the parse.</p>
     * @param message The message.
     */
    public void detail(String message) {
        listener.detail(message);
    }

    /**
     * Emit an info message.
     * <p>Keep me informed about the process of the parse.</p>
     * @param message The message.
     */
    public void info(String message) {
        listener.info(message);
    }

    /**
     * Emit a warning message.
     * <p>Tell if something looks fishy.</p>
     * @param message The message.
     */
    public void warning(String message) {
        listener.warning(message);
    }

    /**
     * Emit an error message.
     * <p>Tell me if there's been an error of some sort.</p>
     * @param message The message.
     */
    public void error(String message) {
        listener.error(message);
    }

    /**
     * Get the current listener.
     * @return the new listener.
     */
    public ParseListener getParseListener() {
        return listener;
    }

    /**
     * Change the listener.
     * @param listener the new listener.
     */
    public void setParseListener(ParseListener listener) {
        this.listener = listener;
    }
}
