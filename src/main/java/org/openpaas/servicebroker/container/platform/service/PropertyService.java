package org.openpaas.servicebroker.container.platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private String caasUrl;

   // 컨테이너 플랫폼 정보
   @Value("${cp.portal.url}")
   private String dashboardUrl;

    @Value("${cp.common-api.url}")
    private String cpCommonApiUrl;

    @Value("${cp.common-api.id}")
    private String commonId;

    @Value("${cp.common-api.password}")
    private String commonPassword;


   // serviceDefinition 정보
   @Value("${serviceDefinition.id}")
   private String serviceDefinitionId;

   // keycloak 정보
   @Value("${keycloak.admin.cp.clusterAdminRole}")
   private String clusterAdminRole;

   public String getContainerPlatformUrl() {
        return caasUrl;
    }

    public void setContainerPlatformUrl(String caasUrl) {
        this.caasUrl = caasUrl;
    }

    public String getCommonId() {
        return commonId;
    }

    public String getCommonPassword() {
        return commonPassword;
    }

    public String getDashboardUrl() { return dashboardUrl; }

    public void setDashboardUrl(String dashboardUrl) {
        this.dashboardUrl = dashboardUrl;
    }

    public String getCpCommonApiUrl() {
        return cpCommonApiUrl;
    }

    public String getServiceDefinitionId() { return serviceDefinitionId; }

    public String getClusterAdminRole() {
        return clusterAdminRole;
    }
}
