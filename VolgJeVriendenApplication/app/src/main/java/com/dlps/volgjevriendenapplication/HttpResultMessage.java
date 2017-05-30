package com.dlps.volgjevriendenapplication;

/**
 * Created by pim on 30-5-17.
 */

public class HttpResultMessage {
    private Integer httpResult;

    public HttpResultMessage(Integer httpResult, String httpMessage) {
        this.httpResult = httpResult;
        this.httpMessage = httpMessage;
    }

    public Integer getHttpResult() {
        return httpResult;
    }

    public String getHttpMessage() {
        return httpMessage;
    }

    private String httpMessage;
}
