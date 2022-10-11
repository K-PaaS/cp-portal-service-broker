package org.openpaas.servicebroker.container.platform.model;


import org.openpaas.servicebroker.container.platform.common.CommonUtils;

/**
 * Keycloak Status model 클래스
 *
 * @author kjhoon
 * @version 1.0
 * @since 2021.08.05
 **/

public class KeycloakUserStatus {
    private String resultCode;
    private String userId;

    public KeycloakUserStatus(){
    }

    public KeycloakUserStatus(String resultCode, String userId) {
        this.resultCode = resultCode;
        this.userId = userId;
    }

    public String getResultCode() {
        return CommonUtils.procReplaceNullValue(resultCode);
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getUserId() {
        return CommonUtils.procReplaceNullValue(userId);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "KeycloakUserStatus{" +
                "resultCode='" + resultCode + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}
