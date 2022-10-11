package org.openpaas.servicebroker.container.platform.model;

/**
 * Users Model 클래스
 *
 * @author kjhoon
 * @version 1.0
 * @since 2021.08.05
 **/

public class Users {

    private String userId;
    private String userAuthId;
    private String cpProviderType;
    private String serviceInstanceId;
    public String userType;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAuthId() {
        return userAuthId;
    }

    public void setUserAuthId(String userAuthId) {
        this.userAuthId = userAuthId;
    }

    public String getCpProviderType() { return cpProviderType; }

    public void setCpProviderType(String cpProviderType) {
        this.cpProviderType = cpProviderType;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getUserType() { return userType; }

    public void setUserType(String userType) { this.userType = userType; }
}
