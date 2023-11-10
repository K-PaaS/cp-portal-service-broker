package org.openpaas.servicebroker.container.platform.service.impl;


import org.apache.http.ssl.TrustStrategy;
import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.openpaas.servicebroker.container.platform.common.CommonStatusCode;
import org.openpaas.servicebroker.container.platform.common.CommonUtils;
import org.openpaas.servicebroker.container.platform.model.Constants;
import org.openpaas.servicebroker.container.platform.model.KeycloakUserStatus;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keycloak Admin Client에 관한 서비스 클래스
 *
 * @author kjhoon
 * @version 20210804
 * @since 20210804
 */
@Service
public class KeycloakAdminClientService {

    @Value("${keycloak.admin.client.grantType}")
    private String grantType;

    @Value("${keycloak.admin.client.username}")
    private String username;

    @Value("${keycloak.admin.client.password}")
    private String password;

    @Value("${keycloak.admin.client.serverUrl}")
    private String serverUrl;

    @Value("${keycloak.admin.client.realm}")
    private String realm;

    @Value("${keycloak.admin.client.clientId}")
    private String clientId;

    @Value("${keycloak.admin.cp.realm}")
    private String cpRealm;

    @Value("${keycloak.admin.cp.IdentityProviderId}")
    private String IdentityProviderId;

    @Value("${keycloak.admin.cp.clusterAdminGroup}")
    private String clusterAdminGroup;


    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClientService.class);

    // build keycloak instance
    public Keycloak getKeycloakInstance() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return Keycloak.getInstance(serverUrl, realm, username, password, clientId, sslContext);
    }




    /**
     * Keycloak 사용자 username을 통해 사용자 상세 정보를 조회한다.
     */
    public UserRepresentation getKeycloakUserDetails(String username) {

        UserRepresentation userRepresentation = new UserRepresentation();

        try {
            Keycloak keycloak = getKeycloakInstance();
            List<UserRepresentation> users = keycloak.realm(cpRealm).users().search(username, true);
            userRepresentation = users.get(0);
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING GET KEYCLOAK USER INFO...");
            logger.info("### EXCEPTION MESSAGE : {}",CommonUtils.loggerReplace(e.getMessage()));
        }

        return userRepresentation;
    }


    /**
     * Keycloak 내 사용자와 그룹의 맵핑 및 해제를 관리한다.
     */
    public void joinClusterAdminGroupToUser(String username) throws ServiceBrokerException {
        Keycloak keycloak = null;
        try {

            keycloak = getKeycloakInstance();
            UserRepresentation ur = getKeycloakUserDetails(username); // get user details
            List<GroupRepresentation> groupList = keycloak.realm(cpRealm).groups().groups(); // find cluster admin group id
            List<GroupRepresentation> cpAdminGroup = groupList.stream().filter(x -> x.getName().matches(clusterAdminGroup)).collect(Collectors.toList());
            GroupRepresentation gr = cpAdminGroup.get(0);
            // join the group to user
            keycloak.realm(cpRealm).users().get(ur.getId()).joinGroup(gr.getId());

        } catch (Exception e) {
            // keycloak.realm(cpRealm).users().get(ur.getId()).leaveGroup(gr.getId());
            logger.info("### EXCEPTION OCCURRED DURING JOIN THE CLUSTER ADMIN GROUP TO USER");
            logger.info("### EXCEPTION MESSAGE : {}",CommonUtils.loggerReplace(e.getMessage()));
            throw new ServiceBrokerException(Constants.USER_SSO_ACCOUNT_CREATE_FAILED_MESSAGE);
        }

    }



    /**
     * 사용자의 UAA Id를 전달받아 Keycloak 사용자 계정을 생성한다.
     * Federated Identity 를 통해 UAA 계정과 Keycloak 계정을 연계한다.
     */
    public KeycloakUserStatus createAdminUserInKeycloak(String username) throws ServiceBrokerException {
        Response response = null;
        KeycloakUserStatus keycloakUserStatus = new KeycloakUserStatus();
        try {
            Keycloak keycloak = getKeycloakInstance();
            // Identity Providers 및 cp-cluster-admin 그룹 join 설정
            UserRepresentation user = setAdminUserDetails(username);
            response = keycloak.realm(cpRealm).users().create(user);
            logger.info("Create Keycloak User Response Status : [{}] {}", CommonUtils.loggerReplace(response.getStatus()),
                    CommonUtils.loggerReplace(response.getStatusInfo().getReasonPhrase()));
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING CREATE KEYCLOAK USER...THROW SERVICE_BROKER_EXCEPTION...");
            logger.info("### EXCEPTION MESSAGE : {}",CommonUtils.loggerReplace(e.getMessage()));
            throw new ServiceBrokerException(Constants.USER_SSO_ACCOUNT_CREATE_FAILED_MESSAGE);
        }

        // 신규 생성된 계정인 경우 keycloak User ID 셋팅
        String resultCode = String.valueOf(response.getStatus());
        if (resultCode.equals(CommonStatusCode.CREATED.getCode())) {
            keycloakUserStatus.setResultCode(CommonStatusCode.CREATED.getCode());
            keycloakUserStatus.setUserId(getKeycloakUserDetails(username).getId());
        }
        return keycloakUserStatus;
    }

    /**
     * Keycloak 사용자 Identity Providers 및 cp-cluster-admin 그룹 join 을 설정한다.
     */
    public UserRepresentation setAdminUserDetails(String username) {

        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(true);

        // Identity Providers 정보 셋팅
        FederatedIdentityRepresentation federatedIdentityRepresentation = new FederatedIdentityRepresentation();
        federatedIdentityRepresentation.setIdentityProvider(IdentityProviderId);
        federatedIdentityRepresentation.setUserId(username);
        federatedIdentityRepresentation.setUserName(username);

        List<FederatedIdentityRepresentation> federatedIdentityRepresentationsList = new ArrayList<>();
        federatedIdentityRepresentationsList.add(federatedIdentityRepresentation);
        user.setFederatedIdentities(federatedIdentityRepresentationsList);

        // cluster-admin-role 그룹 셋팅
        List<String> groups = new ArrayList<>();
        groups.add(clusterAdminGroup);
        user.setGroups(groups);

        return user;
    }


    /**
     * Keycloak 사용자 ID 를 전달받아 Keycloak 사용자 계정을 삭제한다.
     */
    public void deleteKeycloakUser(String userId) {
        Response response = null;
        try {
            Keycloak keycloak = getKeycloakInstance();
            response = keycloak.realm(cpRealm).users().delete(userId);
            logger.info("Delete Keycloak User Response Status : [{}] {}", CommonUtils.loggerReplace(response.getStatus()),
                    CommonUtils.loggerReplace(response.getStatusInfo().getReasonPhrase()));
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING DELETE KEYCLOAK USER ...");
            logger.info("### EXCEPTION MESSAGE : {}",CommonUtils.loggerReplace(e.getMessage()));
        }
    }
}
