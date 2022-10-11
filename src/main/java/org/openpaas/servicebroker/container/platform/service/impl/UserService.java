package org.openpaas.servicebroker.container.platform.service.impl;

import org.keycloak.representations.idm.UserRepresentation;
import org.openpaas.servicebroker.container.platform.common.CommonUtils;
import org.openpaas.servicebroker.container.platform.model.*;
import org.openpaas.servicebroker.container.platform.service.PropertyService;
import org.openpaas.servicebroker.container.platform.service.RestTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/**
 * User에 관한 서비스 클래스
 *
 * @author hyerin
 * @version 20180822
 * @since 2018.08.22
 */
@Service
public class UserService {

    @Autowired
    RestTemplateService restTemplateService;

    @Autowired
    PropertyService propertyService;

    @Autowired
    KeycloakAdminClientService keycloakAdminClientService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);



    /**
     * 컨테이너 플랫폼 관리자 계정 생성 : 컨테이너 플랫폼 API로 요청하여 관리자 계정 생성을 진행한다.
     *
     * @param jpaInstance JpaServiceInstance
     * @return resultStatus
     */
    public String createCpAdmin(JpaServiceInstance jpaInstance) {

        // 사용자 생성에 필요한 필드 값 setting
        Users users = new Users();

        // keycloak 에 생성된 username, id 값 가져오기
        UserRepresentation userRepresentation = keycloakAdminClientService.getKeycloakUserDetails(jpaInstance.getUserId());
        users.setUserId(userRepresentation.getUsername());
        users.setUserAuthId(userRepresentation.getId());

        ResultStatus resultStatus = new ResultStatus();

        try {
            resultStatus = restTemplateService.requestCpApi(users, Constants.URL_API_SIGNUP + Constants.URL_API_SIGNUP_ADMIN_PARAMS + propertyService.getClusterAdminRole(),
                                                            HttpMethod.POST, ResultStatus.class);

            logger.info("## ** Response Admin SignUp Request to API : {}", CommonUtils.loggerReplace(resultStatus.toString()));
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING CREATE ADMIN BY CP API REQUEST");
            return Constants.RESULT_STATUS_FAIL;
        }

        return resultStatus.getResultCode();
    }



    /**
     * 컨테이너 플랫폼 사용자 계정 생성 : 컨테이너 플랫폼 API로 요청하여 사용자 계정 생성을 진행한다.
     *
     * @param jpaInstance JpaServiceInstance
     * @return resultStatus
     */
    public String createCpUser(JpaServiceInstance jpaInstance) {

        // 사용자 생성에 필요한 필드 값 setting
        Users users = new Users();
        users.setServiceInstanceId(jpaInstance.getServiceInstanceId());
        users.setCpProviderType(propertyService.getCpProviderAsService());

        // keycloak에 생성된 username, id 값 가져오기
        UserRepresentation userRepresentation = keycloakAdminClientService.getKeycloakUserDetails(jpaInstance.getUserId());
        users.setUserId(userRepresentation.getUsername());
        users.setUserAuthId(userRepresentation.getId());
        users.setUserType(Constants.AUTH_NAMESPACE_ADMIN);

        ResultStatus resultStatus = new ResultStatus();

        try {
            resultStatus = restTemplateService.requestCpApi(users, Constants.URL_API_SIGNUP, HttpMethod.POST, ResultStatus.class);
            logger.info("## ** Response User SignUp Request to API : {}", CommonUtils.loggerReplace(resultStatus.toString()));
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING CREATE USER BY CP API REQUEST");
            return Constants.RESULT_STATUS_FAIL;
        }

        return resultStatus.getResultCode();
    }


    /**
     * 컨테이너 플랫폼 사용자 계정 삭제 : 컨테이너 플랫폼 COMMON API로 요청하여 사용자 계정 삭제를 진행한다.
     *
     * @param type      the string
     * @param namespace the string
     * @return resultStatus
     */
    public String deleteCpUser(String type, String namespace) {

        ResultStatus resultStatus = new ResultStatus();
        String reqUrl = "";

        if (type.equals(Constants.CONTAINER_PLATFORM_ADMIN_PORTAL)) {
            // 관리자 포탈의 경우 - 클러스터 관리자 계정 삭제
            logger.info("## ** Start Deleting Cluster Admin Account..");
            reqUrl = propertyService.getCpCommonApiUrl() + Constants.URI_CP_COMMON_API_DELETE_CLUSTER_ADMIN
                    .replace("{cluster:.+}", "cp-cluster");
        } else {
            // 사용자 포탈의 경우 - 해당 네임스페이스 사용자 계정 삭제
            logger.info("## ** Start Deleting Namespace [{}] All Users Account..", CommonUtils.loggerReplace(namespace));
            reqUrl = propertyService.getCpCommonApiUrl() + Constants.URI_CP_COMMON_API_DELETE_NAMESPACE_ALL_USERS
                    .replace("{cluster:.+}", "cp-cluster")
                    .replace("{namespace:.+}", namespace);
        }

        try {
            resultStatus = restTemplateService.requestCpCommonApi(reqUrl, HttpMethod.DELETE, ResultStatus.class);
            logger.info("## ** Response to Delete Request to Common API : {}", CommonUtils.loggerReplace(resultStatus.toString()));
        } catch (Exception e) {
            logger.info("### EXCEPTION OCCURRED DURING DELETE USER ACCOUNT BY CP COMMON API REQUEST");
            return Constants.RESULT_STATUS_FAIL;
        }

        return resultStatus.getResultCode();
    }


}
