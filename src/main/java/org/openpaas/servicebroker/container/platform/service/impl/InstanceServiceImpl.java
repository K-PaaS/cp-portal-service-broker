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
 * @author Hyungu Cho
 * @since 2018/07/24
 * @version 20180725
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
     *     1. Request parameters에서 Org GUID, Space GUID과 선택한 Plan의 이름(혹은 GUID)를 추출
     *     2. ServiceInstance 객체 생성 후 Request에서 추출한 데이터 사전 기입
     *     3. KubernetesService를 통해 Namespace, ServiceAccount 생성
     *     4. KubernetesService에서 생성된 Namespace, ServiceAccount
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

        if(request.getServiceDefinition().getId().equalsIgnoreCase(propertyService.getCpBrokerAdminId())) {
            // 관리자포탈 서비스 생성
            createServiceInstanceByCpAdminPortal(instance);
        }
        else {
            // 사용자포탈 서비스 생성
            createServiceInstanceByCpUserPortal(instance);
        }

        return instance;
    }


    /**
     * Service Instance 정보를 테이블에서 가져와 ServiceInstance 객체로 반환한다.
     * TODO : getOne으로 바꾸어도 테스트코드 돌아가는지 확인 (아마될것..?)
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

        //JpaServiceInstance instance = null;

        JpaServiceInstance instance = (JpaServiceInstance) getServiceInstance(request.getServiceInstanceId());

        // DB에 정보가 없을 때
        if (instance == null) {
            String spaceName = "paas-" + request.getServiceInstanceId() + "-caas";
            logger.info("instance is not in DB. check existsNamespace {}", CommonUtils.loggerReplace(spaceName));
            // 실제로 Namespace에도 없을 때
            if (!existsNamespace(spaceName)) {
                logger.info("No more delete thing {}", CommonUtils.loggerReplace(spaceName));
                return null;
            }
            containerPlatformService.deleteNamespace(spaceName);
            return null;
        }

        logger.info("Service Instance Type : [{}]", CommonUtils.loggerReplace(instance.getDashboardType()));

        if(instance.getDashboardType().equals(Constants.CONTAINER_PLATFORM_ADMIN_PORTAL)) {
            // 관리자 포탈의 경우
            instanceRepository.delete(instance);
            userService.deleteCpUser(Constants.CONTAINER_PLATFORM_ADMIN_PORTAL, Constants.NULL_REPLACE_TEXT);
        }
        else {
            // 사용자 포탈의 경우
            containerPlatformService.deleteNamespace(instance.getCaasNamespace());
            instanceRepository.delete(instance);
            userService.deleteCpUser(Constants.CONTAINER_PLATFORM_USER_PORTAL ,instance.getCaasNamespace());
        }

        // unbind(delete binding information)는 구현하지 않기로 결정함.

        return instance;
    }

    /**
     * Service Instance의 정보를 갱신한다. 단, Plan ID, Kubernetes의 계정의 이름(ID), Kubernetes
     * 계정의 엑세스 토큰만 변경이 가능하다.
     *
     * @param request
     *            (update available only these; plan id, account name, account
     *            access token)
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
                } catch(Exception exception) {
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
     * Namespace가 Kubernetes에 존재하는지 확인한다.
     *
     * @param namespace
     * @return
     */
    private boolean existsNamespace(String namespace) {
        return containerPlatformService.existsNamespace(namespace);
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
     * User Portal Service Instance 생성 : 사용자 포탈 서비스 생성 처리
     *
     * @param instance JpaServiceInstance
     * @return
     */
    private void createServiceInstanceByCpUserPortal(JpaServiceInstance instance) throws ServiceInstanceExistsException, ServiceBrokerException
    {

        // 1. 해당 serviceInstance Id 가 이미 등록된 Id 인지 확인
        JpaServiceInstance findInstance = instanceRepository.findByServiceInstanceId(instance.getServiceInstanceId());

        // 1-1. 동일한 instance Id 가 존재한다면 Exception 메세지 발생
        if (findInstance != null) {
            if (findInstance.getServiceInstanceId().equals(instance.getServiceInstanceId())) {
                logger.info("ServiceInstance : {} OR OrgGuid : {} is exist.", CommonUtils.loggerReplace(instance.getServiceInstanceId()), CommonUtils.loggerReplace(instance.getOrganizationGuid()));
                throw new ServiceBrokerException("The service Instance already exists in your organization.");
            } else {
                throw new ServiceInstanceExistsException(instance);
            }
        }

        // 2. 해당 Org 에 이미 등록된 serviceInstance 가 있는지 확인 ( 1 Org 당 1 serviceInstance 생성 가능)
        //    해당 Org 에 이미 등록된 admin serviceInstance 는 제외한다.
        if(instanceRepository.findAllByOrganizationGuidAndDashboardType(instance.getOrganizationGuid(), Constants.CONTAINER_PLATFORM_USER_PORTAL).size() > 0) {
            logger.error("ServiceInstance already exists in your organization: OrganizationGuid : {}, spaceId : {}", CommonUtils.loggerReplace(instance.getOrganizationGuid()), CommonUtils.loggerReplace(instance.getSpaceGuid()));
            throw new ServiceBrokerException("The service Instance already exists in your organization.");
        }

        // 3. 동일한 이름의 namespace 가 이미 생성되어있는지 확인
        String checkSpaceName = "paas-" + instance.getServiceInstanceId().toLowerCase() + "-caas";
        logger.info("Check Namespace Exists : {} ", CommonUtils.loggerReplace(checkSpaceName));
        if(existsNamespace(checkSpaceName))
            throw new ServiceBrokerException("A namespace with the same name is already exists within the cluster.");


        // 4. Uaa ID -> Keycloak Identity Provider Mapping 계정 생성
        KeycloakUserStatus keycloakUserStatus = keycloakAdminClientService.createKeycloakUser(instance.getUserId(), Constants.AUTH_NAMESPACE_ADMIN);
        if(!Constants.KEYCLOAK_CREATE_UESR_STATUS_CODE.contains(keycloakUserStatus.getResultCode())) {
            logger.info("An exception occurred while creating keycloak user");
            throw new ServiceBrokerException("Failed to register Single Sign-On user account.");
        }


        try {
            // 5. k8s 클러스터 내 namespace 생성(namespace, resourcequotas, limitranges, role 생성)
            String namespaceStatus = containerPlatformService.createCpNamespace(instance, getPlan(instance));
            if(!namespaceStatus.equalsIgnoreCase(Constants.RESULT_STATUS_SUCCESS)) {
                logger.info("An exception occurred while creating namespace in k8s");
                throw new ServiceBrokerException("Failed to create namespace in cluster. Please check your cluster!");
            }

            // 6. Instance DB 데이터 추가
            // dashboard url, type 값 설정
            instance.withDashboardUrl(propertyService.getDashboardUrl(instance.getServiceInstanceId()));
            instance.setDashboardType(Constants.CONTAINER_PLATFORM_USER_PORTAL);
            instanceRepository.save(instance);


            // 7. cp user 생성
            String createUserStatus = userService.createCpUser(instance);
            if(!createUserStatus.equalsIgnoreCase(Constants.RESULT_STATUS_SUCCESS)) {
                logger.info("An exception occurred while creating user in container-platform");
                throw new ServiceBrokerException("Failed to register container-platform user account. Please check common DB!");
            }


        }
        catch(Exception exception) {
            logger.error("### Failed to create container-platform user portal service instance... rollback will be execute.");
            // 네임스페이스 삭제
            containerPlatformService.deleteNamespace(instance.getCaasNamespace());
            // instance DB 데이터 삭제
            instanceRepository.delete(instance);
            // 브로커를 통해 신규 생성된 keycloak 계정일 경우 삭제
            if(keycloakUserStatus.getResultCode().equals(CommonStatusCode.CREATED.getCode())) {
                keycloakAdminClientService.deleteKeycloakUser(keycloakUserStatus.getUserId());
            }

            throw new ServiceBrokerException("Failed to create user portal service instance ..Please check your broker DB or common DB!");
        }

    }


    /**
     * Admin Portal Service Instance 생성 : 관리자 포탈 서비스 생성 처리
     *
     * @param instance JpaServiceInstance
     * @return
     */
    private void createServiceInstanceByCpAdminPortal(JpaServiceInstance instance) throws ServiceBrokerException
    {

        // 관리자 포탈은 지정된 한 ORG 에서만 서비스 생성이 가능하다.
        // 1. 관리자 포탈 서비스를 생성하려는 ORG 명이 지정된 ORG 명과 동일한 지 체크
        if(!instance.getOrganizationName().equalsIgnoreCase(propertyService.getCpBrokerAdminOrg())) {
            logger.error("The org name is not [{}] : orgName : {}, spaceName : {}",
                    CommonUtils.loggerReplace(propertyService.getCpBrokerAdminOrg().toLowerCase()), CommonUtils.loggerReplace(instance.getOrganizationName()), CommonUtils.loggerReplace(instance.getSpaceName()));
            throw new ServiceBrokerException("The service Instance can only be created where the organization name is '"+ propertyService.getCpBrokerAdminOrg().toLowerCase()+"'.");
        }

        // 2. 관리자 포탈 서비스 인스턴스가 하나라도 생성되어 있는지 체크 (관리자포탈은 전체 Org 내 1개만 생성 가능)
        if(instanceRepository.existsByDashboardType(Constants.CONTAINER_PLATFORM_ADMIN_PORTAL)) {
            logger.error("ServiceInstance already exists in organization: OrganizationGuid");
            throw new ServiceBrokerException("The service Instance already exists in '"+ propertyService.getCpBrokerAdminOrg().toLowerCase()+ "' organization.");
        }

        KeycloakUserStatus keycloakUserStatus = new KeycloakUserStatus();

        try {
            // 3. Uaa ID -> Keycloak Identity Provider Mapping 계정 생성
             keycloakUserStatus = keycloakAdminClientService.createKeycloakUser(instance.getUserId(), Constants.AUTH_CLUSTER_ADMIN);
            if(!Constants.KEYCLOAK_CREATE_UESR_STATUS_CODE.contains(keycloakUserStatus.getResultCode())) {
                logger.info("An exception occurred while creating keycloak user");
                throw new ServiceBrokerException("Failed to register Single Sign-On user account.");
            }

            //4. 이미 keycloak 내 계정이 존재한다면 cluster-admin-group 맵핑 진행
            if(keycloakUserStatus.getResultCode().equals(CommonStatusCode.CONFLICT.getCode())) {
                String keycloakGroupStatus = keycloakAdminClientService.manageClusterAdminGroupToUser(instance.getUserId(), Constants.TYPE_JOIN);
                if(keycloakGroupStatus.equals(Constants.RESULT_STATUS_FAIL)) {
                    throw new ServiceBrokerException("Failed to register Single Sign-On user account.");
                }
            }

            // 5. Instance DB 데이터 설정
            setInstanceByCpAdminPortal(instance);
            instanceRepository.save(instance);

            // 6. cp admin 생성
            String createAdminStatus = userService.createCpAdmin(instance);
            if(!createAdminStatus.equalsIgnoreCase(Constants.RESULT_STATUS_SUCCESS)) {
                logger.info("An exception occurred while creating admin in container-platform");
                throw new ServiceBrokerException("Failed to register container-platform admin account. Please check common DB!");
            }

        }
        catch(Exception exception) {
            logger.error("### Failed to create container platform admin portal service instance... rollback will be execute.");
            // keycloak instance db data
            instanceRepository.delete(instance);

            // 브로커를 통해 신규 생성된 keycloak 계정일 경우 삭제
            if(keycloakUserStatus.getResultCode().equals(CommonStatusCode.CREATED.getCode())) {
                keycloakAdminClientService.deleteKeycloakUser(keycloakUserStatus.getUserId());
            }

            // 이미 존재한 계정일 경우 cluster-admin-group 맵핑 해제
            if(keycloakUserStatus.getResultCode().equals(CommonStatusCode.CONFLICT.getCode())) {
               keycloakAdminClientService.manageClusterAdminGroupToUser(instance.getUserId(), Constants.TYPE_LEAVE);
            }

            throw new ServiceBrokerException("Failed to create admin portal service instance ..Please check your broker DB or common DB!");
        }

    }

    /**
     * 관리자 포탈 서비스 인스턴스 데이터 설정
     *
     * @param instance JpaServiceInstance
     * @return
     */
    private void setInstanceByCpAdminPortal(JpaServiceInstance instance) {
        instance.withDashboardUrl(propertyService.getAdminDashboardUrl());
        instance.setDashboardType(Constants.CONTAINER_PLATFORM_ADMIN_PORTAL);
        instance.setCaasAccountTokenName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasAccountName(Constants.NULL_REPLACE_TEXT);
        instance.setCaasNamespace(Constants.NULL_REPLACE_TEXT);
    }

}
