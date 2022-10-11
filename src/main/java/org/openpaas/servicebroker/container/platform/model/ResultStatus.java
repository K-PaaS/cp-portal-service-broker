package org.openpaas.servicebroker.container.platform.model;



/**
 * Result Status model 클래스
 *
 * @author kjhoon
 * @version 1.0
 * @since 2021.08.05
 **/

public class ResultStatus {
    private String resultCode;
    private String resultMessage;
    private int httpStatusCode;
    private String detailMessage;
    private String nextActionUrl;

    public String getResultCode() {
        return resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public String getNextActionUrl() {
        return nextActionUrl;
    }

    @Override
    public String toString() {
        return "ResultStatus [" +
                "resultCode='" + resultCode + '\'' +
                ", resultMessage='" + resultMessage + '\'' +
                ", httpStatusCode=" + httpStatusCode +
                ", detailMessage='" + detailMessage + '\'' +
                ", nextActionUrl='" + nextActionUrl + '\'' +
                ']';
    }
}
