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
 * @author kjhoon
 * @since 20210804
 * @version 20210804
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
     * 사용자의 UAA Id를 전달받아 Keycloak 사용자 계정을 생성한다.
     * FederatedIdentity를 통해 UAA 계정과 Keycloak 계정을 연계한다.
     *
     */
    public KeycloakUserStatus createKeycloakUser(String username, String userType){

        KeycloakUserStatus keycloakUserStatus = new KeycloakUserStatus();
        Response response = null;

        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEnabled(true);

            // social login provider 정보 셋팅
            FederatedIdentityRepresentation federatedIdentityRepresentation = new FederatedIdentityRepresentation();
            federatedIdentityRepresentation.setIdentityProvider(IdentityProviderId);
            federatedIdentityRepresentation.setUserId(username);
            federatedIdentityRepresentation.setUserName(username);

            List<FederatedIdentityRepresentation> federatedIdentityRepresentationsList= new ArrayList<>();
            federatedIdentityRepresentationsList.add(federatedIdentityRepresentation);
            user.setFederatedIdentities(federatedIdentityRepresentationsList);


            // 클러스터 관리자의 경우 클러스터 관리자 그룹 셋팅
            if(userType.equals(Constants.AUTH_CLUSTER_ADMIN)) {
                List<String> groups = new ArrayList<>();
                groups.add(clusterAdminGroup);
                user.setGroups(groups);
            }


            // 사용자 생성
            Keycloak keycloak = getKeycloakInstance();
            response = keycloak.realm(cpRealm).users().create(user);
            logger.info("Keycloak User Create Response Status : [{}] {}", CommonUtils.loggerReplace(response.getStatus()),
                    CommonUtils.loggerReplace(response.getStatusInfo().getReasonPhrase()));
        }
        catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING KEYCLOAK USER CREATE");
            keycloakUserStatus.setResultCode(Constants.RESULT_STATUS_FAIL);

            return keycloakUserStatus;
        }


        keycloakUserStatus.setResultCode(String.valueOf(response.getStatus()));

        // 신규 생성된 계정인 경우 keycloak user id 셋팅
        if(String.valueOf(response.getStatus()).equals(CommonStatusCode.CREATED.getCode())) {
            keycloakUserStatus.setUserId(getKeycloakUserDetails(username).getId());
        }

        return keycloakUserStatus;
    }



    /**
     * Keycloak 사용자 ID 를 전달받아 Keycloak 사용자 계정을 삭제한다.
     *
     */
    public String deleteKeycloakUser(String userId) {

        Response response = null;

        try {
            Keycloak keycloak = getKeycloakInstance();
            response = keycloak.realm(cpRealm).users().delete(userId);
            logger.info("Keycloak User Delete Response Status : [{}] {}", CommonUtils.loggerReplace(response.getStatus()),
                    CommonUtils.loggerReplace(response.getStatusInfo().getReasonPhrase()));
        }
        catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING KEYCLOAK USER DELETE");
            return Constants.RESULT_STATUS_FAIL;
        }

        return String.valueOf(response.getStatus());
    }


    /**
     * Keycloak 사용자 username을 통해 사용자 상세 정보를 조회한다.
     *
     */
    public UserRepresentation getKeycloakUserDetails(String username) {

        UserRepresentation userRepresentation = new UserRepresentation();

        try {
            Keycloak keycloak = getKeycloakInstance();
            List<UserRepresentation> users = keycloak.realm(cpRealm).users().search(username, true);
            userRepresentation = users.get(0);
        }
        catch(Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING GET KEYCLOAK USER INFO");
        }

        return userRepresentation;
    }


    /**
     * Keycloak 내 사용자와 그룹의 맵핑 및 해제를 관리한다.
     *
     */
    public String manageClusterAdminGroupToUser(String username, String type) {

        try {
            Keycloak keycloak = getKeycloakInstance();

            // get user
            UserRepresentation ur = getKeycloakUserDetails(username);

            // find cluster admin group id
            List<GroupRepresentation> groupRepresentationList = keycloak.realm(cpRealm).groups().groups();
            List<GroupRepresentation> findCaGroup = groupRepresentationList.stream().filter(x -> x.getName().matches(clusterAdminGroup)).collect(Collectors.toList());
            GroupRepresentation gr = findCaGroup.get(0);

            if(type.equals(Constants.TYPE_JOIN)) {
                logger.info("### JOIN THE CLUSTER ADMIN GROUP TO USER");
                keycloak.realm(cpRealm).users().get(ur.getId()).joinGroup(gr.getId());
            }
            else {
                logger.info("### LEAVE THE CLUSTER ADMIN GROUP TO USER");
                keycloak.realm(cpRealm).users().get(ur.getId()).leaveGroup(gr.getId());
            }

        }
        catch(Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING ADD CLUSTER ADMIN GROUP TO USER");
            return Constants.RESULT_STATUS_FAIL;
        }

        return Constants.RESULT_STATUS_SUCCESS;
    }




}
