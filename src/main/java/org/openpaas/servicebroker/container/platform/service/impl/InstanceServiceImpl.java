package org.openpaas.servicebroker.container.platform.service.impl;

import java.util.List;

import org.openpaas.servicebroker.container.platform.common.CommonStatusCode;
import org.openpaas.servicebroker.container.platform.common.CommonUtils;
import org.openpaas.servicebroker.container.platform.model.Constants;
import org.openpaas.servicebroker.container.platform.model.JpaServiceInstance;
import org.openpaas.servicebroker.container.platform.model.KeycloakUserStatus;
import org.openpaas.servicebroker.container.platform.repo.JpaServiceInstanceRepository;
import org.openpaas.servicebroker.container.platform.service.PropertyService;
import org.openpaas.servicebroker.exception.ServiceBrokerException;
import org.openpaas.servicebroker.exception.ServiceInstanceExistsException;
import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.DeleteServiceInstanceRequest;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.openpaas.servicebroker.model.UpdateServiceInstanceRequest;
import org.openpaas.servicebroker.service.CatalogService;
import org.openpaas.servicebroker.service.ServiceInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service Instance를 관리하기 위한 Service 클래스이다.
 *
 * @author jhoon
 * @version 1.0
 * @since 20221013
 */
@Service
public class InstanceServiceImpl implements ServiceInstanceService {

    @Autowired
    private CatalogService catalog;

    @Autowired
    private JpaServiceInstanceRepository instanceRepository;

    @Autowired
    ContainerPlatformService containerPlatformService;

    @Autowired
    PropertyService propertyService;

    @Autowired
    UserService userService;

    @Autowired
    KeycloakAdminClientService keycloakAdminClientService;

    private static final Logger logger = LoggerFactory.getLogger(InstanceServiceImpl.class);

    /**
     * <p>
     * Service Instance 생성 : request 온 내용을 바탕으로 다음의 내용을 구성
     * </p>
     *
     * <pre>
     *     1. Request parameters에서 Owner, Org GUID, Space GUID, 선택한 Plan의 이름(혹은 GUID)를 추출
     *     2. ServiceInstance 객체 생성 후 Request 에서 추출한 데이터 사전 기입
     *     3. CP Portal에 'SUPER_ADMIN' 권한 사용자 미등록 시 keycloakAdminClientService 를 통해 keycloak 사용자 생성
     *     4. Broker DB 에 Instance 데이터 저장
     * </pre>
     *
     * @param request
     * @return
     */
    @Override
    public JpaServiceInstance createServiceInstance(final CreateServiceInstanceRequest request) throws ServiceInstanceExistsException, ServiceBrokerException {

        logger.info("Create Kubernetes service instance : {}", CommonUtils.loggerReplace(request.getServiceInstanceId()));
        logger.info("Request Parameter Values : {}", CommonUtils.loggerReplace(request.getParameters().toString()));

        JpaServiceInstance instance = (JpaServiceInstance) new JpaServiceInstance(request);
        createServiceInstanceByCpPortal(instance);     // CP Portal 서비스 생성
        return instance;
    }


    /**
     * Service Instance 정보를 테이블에서 가져와 ServiceInstance 객체로 반환한다.
     * TODO : getOne으로 바꾸어도 테스트코드 돌아가는지 확인 (아마될것..?)
     *
     * @param serviceInstanceId
     * @return
     */
    @Override
    public ServiceInstance getServiceInstance(String serviceInstanceId) {
        return instanceRepository.findByServiceInstanceId(serviceInstanceId);
    }

    @Override
    public ServiceInstance getOperationServiceInstance(String var1) {
        return null;
    }

    /**
     * 외부에서의 요청으로 전달받은 service instance id 등을 비교하여 Service Instance ID 정보를 찾은 다음,
     * Caas의 namespace의 삭제와 service instance 정보를 차례대로 삭제한다.
     *
     * @param request
     * @return
     * @throws ServiceBrokerException
     */
    @Override
    public ServiceInstance deleteServiceInstance(DeleteServiceInstanceRequest request) throws ServiceBrokerException {
        logger.info("Delete Kubernetes service instance : {}", CommonUtils.loggerReplace(request.getServiceInstanceId()));
        JpaServiceInstance instance = (JpaServiceInstance) getServiceInstance(request.getServiceInstanceId());
        try {
            instanceRepository.delete(instance);
        } catch (Exception e) {
            throw new ServiceBrokerException("Failed to delete service instance ..Please check your broker DB!");
        }
        return instance;
    }

    /**
     * Service Instance의 정보를 갱신한다. 단, Plan ID, Kubernetes의 계정의 이름(ID), Kubernetes
     * 계정의 엑세스 토큰만 변경이 가능하다.
     *
     * @param request (update available only these; plan id, account name, account
     *                access token)
     * @return ServiceInstance
     */
    @Override
    public ServiceInstance updateServiceInstance(UpdateServiceInstanceRequest request) throws ServiceBrokerException {
        logger.info("Update Kubernetes service instance : {}", CommonUtils.loggerReplace(request.getServiceInstanceId()));

        JpaServiceInstance findInstance = instanceRepository.findByServiceInstanceId(request.getServiceInstanceId());
        if (null == findInstance)
            throw new ServiceBrokerException("Cannot find service instance id : " + request.getServiceInstanceId());

        JpaServiceInstance instance = new JpaServiceInstance(request);

        if (findInstance.equals(instance)) {
            String planId = instance.getPlanId();
            logger.debug("Plan ID : {}", CommonUtils.loggerReplace(planId));

            if (null != planId) {
                // 지정한 Plan ID가 실제 있는 Plan의 UUID가 맞는지 유효성 확인.
                logger.info("Change Plan : {} -> {}", CommonUtils.loggerReplace(findInstance.getPlanId()), CommonUtils.loggerReplace(instance.getPlanId()));
                Plan oldPlan = this.getPlan(findInstance);
                Plan newPlan = this.getPlan(instance);
                if (oldPlan.getWeight() > newPlan.getWeight())
                    throw new ServiceBrokerException("Cannot change lower plan. (current: " + oldPlan.getName() + " / new: " + newPlan.getName() + ")");

                findInstance.setPlanId(planId);
                containerPlatformService.changeResourceQuota(findInstance.getCaasNamespace(), newPlan);

                // ---------------------------------------------------------------------------- 추가
                try {
                    logger.info("Save data broker DB");
                    instanceRepository.save(findInstance);
                } catch (Exception exception) {
                    logger.error("somthing wrong! update rollback will be execute.");
                    containerPlatformService.changeResourceQuota(findInstance.getCaasNamespace(), oldPlan);
                    findInstance.setPlanId(oldPlan.getId());
                    instanceRepository.save(findInstance);
                    throw new ServiceBrokerException("Please check your Network state");
                }
                // ---------------------------------------------------------------------------- 추가 끝
            }
//            instanceRepository.save(findInstance);

        }

        return findInstance;
    }


    /**
     * 클래스의 메소드에서 Plan 정보가 필요한 경우, Service instance의 plan id를 이용해 plan을 찾아준다.
     *
     * @param instance
     * @return Plan
     * @throws ServiceBrokerException
     */
    private Plan getPlan(JpaServiceInstance instance) throws ServiceBrokerException {
        logger.info("Get plan info. from Catalog service in this broker.");
        ServiceDefinition serviceDefinition = catalog.getServiceDefinition(instance.getServiceDefinitionId());
        final List<Plan> plans = serviceDefinition.getPlans();
        Plan plan = null;
        for (int size = plans.size(), i = 0; i < size; i++) {
            if (plans.get(i).getId().equals(instance.getPlanId())) {
                plan = plans.get(i);
                return plan;
            }
        }

        throw new ServiceBrokerException("Cannot find plan using plan id into service instance info.");
    }


    /**
     * Container Platform Portal Service Instance 생성 : 포탈 서비스 생성 처리
     *
     * @param instance JpaServiceInstance
     * @return
     */
    private void createServiceInstanceByCpPortal(JpaServiceInstance instance) throws ServiceInstanceExistsException, ServiceBrokerException {

        // 1. 해당 SERVICE_INSTANCE ID 등록 여부 확인
        JpaServiceInstance findInstance = instanceRepository.findByServiceInstanceId(instance.getServiceInstanceId());

        // 1-1. 동일한 SERVICE_INSTANCE ID 가 존재한다면 Exception 발생
        if (findInstance != null) {
            logger.info("ServiceInstance : {} OR OrgGuid : {} is exist.", CommonUtils.loggerReplace(instance.getServiceInstanceId()), CommonUtils.loggerReplace(instance.getOrganizationGuid()));
            throw new ServiceInstanceExistsException(instance);
        }

        // 2. 해당 ORG 에 이미 등록된 CP Portal 서비스가 있는지 확인 (1 ORG 당 1 SERVICE_INSTANCE ID 생성 가능)
        if (instanceRepository.existsByOrganizationGuid(instance.getOrganizationGuid())) {
            logger.error("ServiceInstance already exists in your organization: OrganizationGuid : {}, spaceId : {}", CommonUtils.loggerReplace(instance.getOrganizationGuid()), CommonUtils.loggerReplace(instance.getSpaceGuid()));
            throw new ServiceBrokerException("The service Instance already exists in your organization.");
        }

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
        instance.withDashboardUrl(propertyService.getDashboardUrl());
        instance.setDashboardType(Constants.NULL_REPLACE_TEXT);
        instance.setCaasAccountTokenName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasAccountName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasNamespace(Constants.NULL_REPLACE_TEXT);
    }

}
