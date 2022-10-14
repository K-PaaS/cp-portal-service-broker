package org.openpaas.servicebroker.container.platform.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpaas.servicebroker.model.Catalog;
import org.openpaas.servicebroker.model.Plan;
import org.openpaas.servicebroker.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 * Spring boot 구동시 Catalog API 에서 사용하는 Catalog Bean을 생성하는 클래스
 *
 * @author Hyerin
 * @author Hyungu Cho
 * @since 2018.07.24
 * @version 20180724
 */
@Component
public class CatalogConfig {
    ///////////////////////////
    //// START PLAN COMMON ////
    ///////////////////////////
    @Value("${serviceDefinition.id}")
    private String SERVICEDEFINITION_ID;

    @Value("${serviceDefinition.name}")
    private String SERVICEDEFINITION_NAME;

    @Value("${serviceDefinition.desc}")
    private String SERVICEDEFINITION_DESC;

    @Value("${serviceDefinition.bindable}")
    private String SERVICEDEFINITION_BINDABLE_STRING;

    @Value("${serviceDefinition.planupdatable}")
    private String SERVICEDEFINITION_PLANUPDATABLE_STRING;

    @Value("${serviceDefinition.image_url}")
    private String SERVICEDEFINITION_IMAGE_URL;

    @Value("#{'${serviceDefinition.tags}'.split(',')}")
    private List<String> SERVICEDEFINITION_TAGS;


    ///////////////////////////
    ////    START PLAN 1  ////
    ///////////////////////////
    @Value("${serviceDefinition.plan1.id}")
    private String SERVICEDEFINITION_PLAN1_ID;

    @Value("${serviceDefinition.plan1.name}")
    private String SERVICEDEFINITION_PLAN1_NAME;

    @Value("${serviceDefinition.plan1.desc}")
    private String SERVICEDEFINITION_PLAN1_DESC;


    /**
     * Catalog bean 객체 생성 후 반환
     * @author hyerin
     * @return Catalog
     */
    @Bean
    public Catalog catalog() {
        boolean SERVICEDEFINITION_BINDABLE = false;
        boolean SERVICEDEFINITION_PLANUPDATABLE = false;
        ServiceDefinition serviceDefinition = null;

        if(SERVICEDEFINITION_BINDABLE_STRING.toUpperCase().trim().equals("TRUE"))
            SERVICEDEFINITION_BINDABLE = true;

        if(SERVICEDEFINITION_PLANUPDATABLE_STRING.toUpperCase().trim().equals("TRUE"))
            SERVICEDEFINITION_PLANUPDATABLE = true;

            serviceDefinition =  new ServiceDefinition(
                    SERVICEDEFINITION_ID,
                    SERVICEDEFINITION_NAME,
                    SERVICEDEFINITION_DESC,
                    SERVICEDEFINITION_BINDABLE, // bindable
                    SERVICEDEFINITION_PLANUPDATABLE, // updatable
                    Arrays.asList(
                            new Plan( SERVICEDEFINITION_PLAN1_ID,
                                    SERVICEDEFINITION_PLAN1_NAME,
                                    SERVICEDEFINITION_PLAN1_DESC)),
                    SERVICEDEFINITION_TAGS,
                    getServiceDefinitionMetadata(),
                    null,
                    null );

        return new Catalog( Arrays.asList(serviceDefinition) );

    }

    /**
     * Service Definition Metadata 객체를 생성 <br>
     * (Used by Pivotal CF console)
     * @return Map&lt;String, Object&gt;
     */
    private Map<String, Object> getServiceDefinitionMetadata() {
        Map<String, Object> sdMetadata = new HashMap<String, Object>();
        sdMetadata.put("displayName", "Caas Kubernetes Service");
        sdMetadata.put("imageUrl", SERVICEDEFINITION_IMAGE_URL);
        sdMetadata.put("longDescription", "Shared Kubernetes service. New service creates new namespace(non-existed)," +
            " new a service account in namespace, and binding role for service account");
        sdMetadata.put("providerDisplayName", "CNCF Kubernetes");
        sdMetadata.put("documentationUrl", "https://caas.io");
        sdMetadata.put("supportUrl", "https://caas.io");
        return sdMetadata;
    }



    /**
     * Plan의 Costs 정보를 Map 객체의 리스트 형태로 반환
     * @param planType
     * @return Map&lt;String, Object&gt;
     */
    private List<Map<String, Object>> getCosts(String planType) {
        Map<String, Object> costsMap = new HashMap<>();
        Map<String, Object> amount = new HashMap<>();

        switch (planType) {
            case "A":
                amount.put("usd", 0.0);
                costsMap.put("amount", amount);
                costsMap.put("unit", "FREE");
                break;
            case "B":
                amount.put("usd", 0.0);
                costsMap.put("amount", amount);
                costsMap.put("unit", "FREE");
                break;
            case "C":
                amount.put("usd", 0.0);
                costsMap.put("amount", amount);
                costsMap.put("unit", "FREE");
                break;
            default:
                amount.put("usd", 0.0);
                costsMap.put("amount", amount);
                costsMap.put("unit", "FREE");
                break;
        }

        return Collections.singletonList(costsMap);
    }



 /*   *//**
     * Plan의 Bullets 정보를 담은 객체를 반환
     * @param planType
     * @return List&lt;String&gt;
     *//*
    private List<String> getBullets(String planType) {
        if (planType.equals("A")) {
            return Arrays.asList(SERVICEDEFINITION_PLAN1_DESC);
        } else if (planType.equals("B")) {
            return Arrays.asList(SERVICEDEFINITION_PLAN2_DESC);
        } else if (planType.equals("C")) {
            return Arrays.asList(SERVICEDEFINITION_PLAN3_DESC);
        }

        return Arrays.asList(SERVICEDEFINITION_PLAN1_DESC);
    }*/

    /*    *//**
     * Costs, bullets 정보를 포함한 Plan metadata 객체를 생성
     * @param planType
     * @return Map&lt;String, Object&gt;
     *//*
    private Map<String, Object> getPlanMetadata(String planType) {
        Map<String, Object> planMetadata = new HashMap<>();
        planMetadata.put("costs", getCosts(planType));
        planMetadata.put("bullets", getBullets(planType));

        return planMetadata;
    }*/
}