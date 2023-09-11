package org.openpaas.servicebroker.model.fixture;

import org.openpaas.servicebroker.model.CreateServiceInstanceRequest;
import org.openpaas.servicebroker.model.ServiceInstance;
import org.servicebroker.apiplatform.common.TestConstants;

public class ServiceInstanceFixture {

//    public static List<ServiceInstance> getAllServiceInstances() {
//        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
//        instances.add(getServiceInstance());
//        //instances.add(getServiceInstanceTwo());
//        return instances;
//    }
    
    public static ServiceInstance getServiceInstance() {
        return new ServiceInstance(new CreateServiceInstanceRequest(
                "service-one-id", 
                "plan-one-id", 
                DataFixture.getOrgOneGuid(), 
                DataFixture.getSpaceOneGuid()).withServiceInstanceId("service-instnce-one-id"))
        		.withDashboardUrl(TestConstants.DASHBOARD_URL + "/"+ TestConstants.SV_INSTANCE_ID_001);
                
    }
    
//    public static ServiceInstance getServiceInstanceTwo() {
//        return new ServiceInstance(new CreateServiceInstanceRequest(
//                "service-two-id", 
//                "plan-two-id", 
//                DataFixture.getOrgOneGuid(), 
//                DataFixture.getSpaceOneGuid()).withServiceInstanceId("service-instnce-two-id"))
//        		.withDashboardUrl(TestConstants.DASHBOARD_URL + "/"+ TestConstants.SV_INSTANCE_ID_002);
//
//    }
//    
//    public static String getServiceInstanceId() {
//        return "service-instance-id";
//    }
//    
//    public static CreateServiceInstanceRequest getCreateServiceInstanceRequest() {
//        ServiceDefinition service = ServiceFixture.getService();
//        return new CreateServiceInstanceRequest(
//                service.getId(), 
//                service.getPlans().get(0).getId(),
//                DataFixture.getOrgOneGuid(),
//                DataFixture.getSpaceOneGuid()
//        );
//    }
//    
//    public static String getCreateServiceInstanceRequestJson() throws JsonGenerationException, JsonMappingException, IOException {
//         return DataFixture.toJson(getCreateServiceInstanceRequest());
//    }
//        
//    public static CreateServiceInstanceResponse getCreateServiceInstanceResponse() {
//        return new CreateServiceInstanceResponse(getServiceInstance());
//    }
//
//    public static String getUpdateServiceInstanceRequestJson() throws JsonGenerationException,
//    JsonMappingException, IOException {
//        return DataFixture.toJson(getUpdateServiceInstanceRequest());
//    }
//    
//    public static UpdateServiceInstanceRequest getUpdateServiceInstanceRequest() {
//        ServiceDefinition service = ServiceFixture.getService();
//        return new UpdateServiceInstanceRequest(service.getPlans().get(0).getId());
//    }


}
