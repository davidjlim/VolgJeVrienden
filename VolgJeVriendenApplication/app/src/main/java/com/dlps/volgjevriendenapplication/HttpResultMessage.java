package com.dlps.volgjevriendenapplication;

/**
 * Small struct class to hold an Httpmessage and its resultcode
 * Created by pim on 30-5-17.
 */

public class HttpResultMessage {
    /**
     * The Http resultcode
     */
    private Integer httpResult;
    /**
     * The Http message
     */
    private String httpMessage;

    /**
     * Constructor
     * @param httpResult the http resultcode
     * @param httpMessage the http message
     */
    public HttpResultMessage(Integer httpResult, String httpMessage) {
        this.httpResult = httpResult;
        this.httpMessage = httpMessage;
    }

    /**
     * Gets the held Http resultcode
     * @return
     */
    public Integer getHttpResult() {
        return httpResult;
    }

    /**
     * Gets the held Http message
     * @return
     */
    public String getHttpMessage() {
        return httpMessage;
    }
}
