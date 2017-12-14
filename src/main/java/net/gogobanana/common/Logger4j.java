package net.gogobanana.common;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Created by josephn on 5/12/2017.
 * Generic logger implementation used by all classes
 */
public class Logger4j implements Logger {

    private LambdaLogger lambdaLogger = null;
    private final org.apache.log4j.Logger rootLogger;
    // add other logger here

    /**
     * Create a logger that can handle either logging all to lambda or (logging to cloudwatch and/or stdout/stderr)
     * @param lambdaLogger
     */
    public Logger4j(LambdaLogger lambdaLogger){
        if (lambdaLogger != null)
            this.lambdaLogger = lambdaLogger;
        rootLogger = org.apache.log4j.Logger.getRootLogger();

    }

    public Logger4j(){
        rootLogger = org.apache.log4j.Logger.getRootLogger();
    }

    private String buildMessage(Throwable e, Boolean isError, String message, Object... params) {
        StringBuilder sb = new StringBuilder();
        if (isError)
            sb.append("ERROR:");

        if (params != null && params.length > 0){
            try {
                sb.append(String.format(message, params));
            } catch (Exception ex){
                sb.append(message);
                for (int i = 0; i < params.length; i++){
                    if (params[i] != null) {
                        sb.append(" :: ");
                        sb.append(params[i]);
                    }
                    else {
                        sb.append(" :: (null}");
                    }
                }
            }
        }
        else {
            sb.append(message);
        }
        sb.append("\n");

        if (e != null){
            sb.append(" \n" + e.getMessage() + " \n");
            sb.append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
        }
        return sb.toString();
    }

    private void processLogMessage(String formattedMessage){

        try {
            if (lambdaLogger != null)
                lambdaLogger.log(formattedMessage);
            else
                rootLogger.info(formattedMessage);
        } catch(Exception ex){

        }
    }

    private void processErrorMessage(String formattedMessage){
        try {
            if (lambdaLogger != null)
                lambdaLogger.log(formattedMessage);
            else
                rootLogger.error(formattedMessage);
        } catch (Exception ex){

        }
    }

    private void processErrorMessage(String formattedMessage, Throwable e){
        try {
            if (lambdaLogger != null)
                lambdaLogger.log(formattedMessage);
            else
                rootLogger.error(formattedMessage, e);
        } catch (Exception ex){

        }
    }

    @Override
    public void log(String message) {
        String formattedMessage = this.buildMessage(null,false,message);
        this.processLogMessage(formattedMessage);
    }

    @Override
    public void log(String format, Object... params) {
        String formattedMessage = this.buildMessage(null,false,format,params);
        this.processLogMessage(formattedMessage);
    }

    @Override
    public void error(String message) {
        String formattedMessage = this.buildMessage(null,true,message);
        this.processErrorMessage(formattedMessage);
    }

    @Override
    public void error(String format, Object... params) {
        String formattedMessage = this.buildMessage(null,true,format,params);
        this.processErrorMessage(formattedMessage);
    }

    @Override
    public void error(Throwable e, String message) {
        String formattedMessage = this.buildMessage(e,true,message);
        this.processErrorMessage(formattedMessage,e);
    }

    @Override
    public void error(Throwable e, String format, Object... params) {
        String formattedMessage = this.buildMessage(e,true,format,params);
        this.processErrorMessage(formattedMessage,e);
    }


}

