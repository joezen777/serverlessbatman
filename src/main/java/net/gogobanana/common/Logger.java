package net.gogobanana.common;


import java.lang.String;

/**
 * Created by josephn on 5/12/2017.
 * Logger interface used by all classes
 *
 */
public interface Logger {

    /**
     * Send the message to the log and the stdout stream
     * @param message message to log
     */
    void log(String message);

    /**
     * Send the formatted message to the log and the stdout stream
     * @param format String.format template of message to log
     * @param params Arguments for String.format template
     * @see java.lang.String#format(String, Object...)
     */
    void log(String format, Object... params);

    void error(String message);

    /**
     * Send formatted message to the log and the stderr stream
     * @param format String.format template of error message to log
     * @param params Arguments for String.format template
     * @see java.lang.String#format(String, Object...)
     */
    void error(String format, Object... params);

    /**
     * Send the exception and message to the log and the stderr stream
     * @param e Exception behind error message
     * @param message Error message to log
     */
    void error(Throwable e, String message);

    /**
     * Send the exception and formatted message to the log and the stderr stream
     * @param e Exception behind error message
     * @param format String.format template of error message to log
     * @param params Arguments for String.format template
     * @see java.lang.String#format(String, Object...)
     */
    void error(Throwable e, String format, Object... params);

}

