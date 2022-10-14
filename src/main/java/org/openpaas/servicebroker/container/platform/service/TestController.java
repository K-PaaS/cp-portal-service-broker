package org.openpaas.servicebroker.container.platform.service;

import org.openpaas.servicebroker.container.platform.common.CommonStatusCode;
import org.openpaas.servicebroker.container.platform.model.Constants;
import org.openpaas.servicebroker.container.platform.model.JpaServiceInstance;
import org.openpaas.servicebroker.container.platform.model.KeycloakUserStatus;
import org.openpaas.servicebroker.container.platform.repo.JpaServiceInstanceRepository;
import org.openpaas.servicebroker.container.platform.service.impl.KeycloakAdminClientService;
import org.openpaas.servicebroker.container.platform.service.impl.UserService;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Clusters Controller 클래스
 *
 * @author hrjin
 * @version 1.0
 * @since 2020.11.04
 **/
@RestController
public class TestController {


    private final UserService userService;
    private final KeycloakAdminClientService keycloakAdminClientService;
    private final PropertyService propertyService;
    private final JpaServiceInstanceRepository instanceRepository;

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    public TestController(UserService userService, KeycloakAdminClientService keycloakAdminClientService,
                          PropertyService propertyService, JpaServiceInstanceRepository instanceRepository) {
        this.userService = userService;
        this.keycloakAdminClientService = keycloakAdminClientService;
        this.propertyService = propertyService;
        this.instanceRepository = instanceRepository;
    }


    @GetMapping(value = "/createKeycloakUser/{username}")
    public void getTest(@PathVariable(value = "username") String username) throws ServiceInstanceExistsException, ServiceBrokerException {
        JpaServiceInstance instance = new JpaServiceInstance();
        instance.setUserId(username);
        KeycloakUserStatus keycloakUserStatus = new KeycloakUserStatus();

        // 3. CP Portal 에 'SUPER-ADMIN' 권한의 사용자 등록 여부 확인
        if (!userService.isExistsCpPortalAdmin()) {
            // 'SUPER-ADMIN' 권한의 사용자가 없는 경우, Keycloak 계정 생성
            //  - uaa <-> keycloak Identity Providers 설정
            //  - cp admin group join
            logger.info("[1] Create cp admin user in Keycloak because there is currently no admin in the cp portal...");
            keycloakUserStatus = keycloakAdminClientService.createAdminUserInKeycloak(instance.getUserId());
        }

        try {
            // 4. Broker DB 에 Instance 정보 생성
            logger.info("[2] Save Instance data in Broker DB...");
            setInstanceByCpPortal(instance);
            instanceRepository.save(instance);

        } catch (Exception exception) {
            // Exception 발생 시 instance 데이터 삭제 및 keycloak 계정 삭제
            logger.info("### EXCEPTION OCCURRED DURING SAVE INSTANCE DATA IN BROKER DB...");
            logger.info("### DELETE INSTANCE DATA IN BROKER DB...");
            instanceRepository.delete(instance);

            logger.info("### DELETE JUST CREATED KEYCLOAK USER...");
            if (keycloakUserStatus.getResultCode().equals(CommonStatusCode.CREATED.getCode())) {
                System.out.println("delete keycloak user: " + keycloakUserStatus.getUserId());
                keycloakAdminClientService.deleteKeycloakUser(keycloakUserStatus.getUserId());
            }

            throw new ServiceBrokerException("Failed to create portal service instance ..Please check your broker DB or common DB!");
        }

    }


    /**
     * 관리자 포탈 서비스 인스턴스 데이터 설정
     *
     * @param instance JpaServiceInstance
     * @return
     */
    private void setInstanceByCpPortal(JpaServiceInstance instance) {
        ////
        instance.setServiceInstanceId(Constants.NULL_REPLACE_TEXT);
        instance.setUserId(Constants.NULL_REPLACE_TEXT);
        instance.setPlanId(Constants.NULL_REPLACE_TEXT);
        instance.setOrganizationGuid(Constants.NULL_REPLACE_TEXT);
        instance.setServiceDefinitionId(Constants.NULL_REPLACE_TEXT);
        instance.setSpaceGuid(Constants.NULL_REPLACE_TEXT);

        ////
        instance.withDashboardUrl(propertyService.getDashboardUrl());
        instance.setDashboardType(Constants.NULL_REPLACE_TEXT);
        instance.setCaasAccountTokenName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasAccountName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasNamespace(Constants.NULL_REPLACE_TEXT);
    }

}
