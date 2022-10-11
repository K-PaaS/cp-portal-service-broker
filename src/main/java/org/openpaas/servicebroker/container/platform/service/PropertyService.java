package org.openpaas.servicebroker.container.platform.service;

import org.openpaas.servicebroker.container.platform.model.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Property 변수를 담은 서비스 클래스
 *
 * @author Hyerin
 * @version 20180822
 * @since 2018.08.22
 */
@Service
public class PropertyService {

    /**
     * The caas master host url.
     */

   // 클러스터 정보
    @Value("${k8s.url}")
    private String caasUrl;

    @Value("${k8s.auth_bearer_token}")
    private String authBearer;

    @Value("${k8s.cluster.command}")
    private String caasClusterCommand;

    @Value("${k8s.cluster.exit-code}")
    private String caasClusterExitCode;



   // 컨테이너 플랫폼 정보
   @Value("${cp.portal.url}")
   private String dashboardUrl;

    @Value("${cp.portal.url}")
    private String adminDashboardUrl;

    @Value("${cp.common-api.url}")
    private String cpCommonApiUrl;

    @Value("${cp.common-api.id}")
    private String commonId;

    @Value("${cp.common-api.password}")
    private String commonPassword;

    @Value("${cp.api.url}")
    private String cpApiUrl;

    @Value("${cp.role.list}")
    List<String> rolesList;

    @Value("${cp.role.init}")
    private String initRole;

    @Value("${cp.role.admin}")
    private String adminRole;

    @Value("${cp.provide-as-service}")
    private String cpProviderAsService;

    @Value("${cp.broker.admin.id}")
    private String cpBrokerAdminId;

    @Value("${cp.broker.admin.org}")
    private String cpBrokerAdminOrg;

    @Value("${cp.broker.user.id}")
    private String cpBrokerUserId;


    // 도커 레지스트리 정보
    @Value("${private.docker.registry.auth.id}")
    private String authId;

    @Value("${private.docker.registry.auth.password}")
    private String authPassword;

    @Value("${private.docker.registry.uri}")
    private String privateDockerUri;

    @Value("${private.docker.registry.port}")
    private String privateDockerPort;

    @Value("${private.docker.registry.secret.name}")
    private String privateDockerSecretName;


   // serviceDefinition 정보
   @Value("${serviceDefinition.id}")
   private String serviceDefinitionId;

   // keycloak 정보
   @Value("${keycloak.admin.cp.clusterAdminRole}")
   private String clusterAdminRole;




    public String getContainerPlatformClusterCommand() {
        return caasClusterCommand;
    }

    public void setContainerPlatformClusterCommand(String caasClusterCommand) {
        this.caasClusterCommand = caasClusterCommand;
    }

    public String getContainerPlatformClusterExitCode() {
        return caasClusterExitCode;
    }

    public void setContainerPlatformClusterExitCode(String caasClusterExitCode) {
        this.caasClusterExitCode = caasClusterExitCode;
    }

    public String getContainerPlatformUrl() {
        return caasUrl;
    }

    public void setContainerPlatformUrl(String caasUrl) {
        this.caasUrl = caasUrl;
    }

    public String getCommonId() {
        return commonId;
    }

    public void setCommonId(String commonId) {
        this.commonId = commonId;
    }

    public String getCommonPassword() {
        return commonPassword;
    }

    public void setCommonPassword(String commonPassword) {
        this.commonPassword = commonPassword;
    }

    public String getDashboardUrl(String serviceInstanceId) {
        return dashboardUrl + Constants.DASHBOARD_URI_PARAMS.replace("{sid:.+}", serviceInstanceId);
    }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getPrivateDockerUri() {
        return privateDockerUri;
    }

    public void setPrivateDockerUri(String privateDockerUri) {
        this.privateDockerUri = privateDockerUri;
    }

    public String getPrivateDockerPort() {
        return privateDockerPort;
    }

    public void setPrivateDockerPort(String privateDockerPort) {
        this.privateDockerPort = privateDockerPort;
    }

    public String getPrivateDockerSecretName() {
        return privateDockerSecretName;
    }

    public void setPrivateDockerSecretName(String privateDockerSecretName) {
        this.privateDockerSecretName = privateDockerSecretName;
    }

    public String getAuthBearer() {
        return authBearer;
    }

    public void setAuthBearer(String authBearer) {
        this.authBearer = authBearer;
    }

    public List<String> getRolesList() {
        return rolesList;
    }

    public String getInitRole() {
        return initRole;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public String getCpApiUrl() {
        return cpApiUrl;
    }

    public String getCpCommonApiUrl() {
        return cpCommonApiUrl;
    }
    public String getCpProviderAsService() {
        return cpProviderAsService;
    }

    public String getCpBrokerAdminId() { return cpBrokerAdminId; }

    public String getCpBrokerUserId() { return cpBrokerUserId; }

    public String getCpBrokerAdminOrg() {
        return cpBrokerAdminOrg;
    }

    public String getAdminDashboardUrl() {
        return adminDashboardUrl + "?sessionRefresh=true";
    }

    public String getServiceDefinitionId() { return serviceDefinitionId; }

    public String getClusterAdminRole() {
        return clusterAdminRole;
    }
}
