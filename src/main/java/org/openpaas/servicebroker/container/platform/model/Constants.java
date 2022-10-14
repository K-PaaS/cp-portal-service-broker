package org.openpaas.servicebroker.container.platform.model;

import org.openpaas.servicebroker.container.platform.common.CommonStatusCode;

import java.util.Arrays;
import java.util.List;

public class Constants {
    
    public static final String TOKEN_KEY = "caas_admin";
    public static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    public static final String RESULT_STATUS_FAIL = "FAIL";

    public static final String AUTH_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String AUTH_CLUSTER_ADMIN = "CLUSTER_ADMIN";
    public static final String AUTH_NAMESPACE_ADMIN = "NAMESPACE_ADMIN";
    public static final String AUTH_USER = "USER";

    public static final String CONTAINER_PLATFORM_USER_PORTAL = "user";
    public static final String CONTAINER_PLATFORM_ADMIN_PORTAL = "admin";

    public static final String TYPE_JOIN = "join";
    public static final String TYPE_LEAVE = "leave";

    public static final String NULL_REPLACE_TEXT = "-";


    public static final String URL_API_SIGNUP = "/signUp";
    public static final String URL_API_SIGNUP_ADMIN_PARAMS = "?isAdmin=true&param=";

    public static final String URI_CP_COMMON_API_DELETE_NAMESPACE_ALL_USERS = "/clusters/{cluster:.+}/namespaces/{namespace:.+}/users";
    public static final String URI_CP_COMMON_API_DELETE_CLUSTER_ADMIN = "/clusters/{cluster:.+}/admin/delete";

    public static final String URI_CP_COMMON_API_CHECK_EXISTS_ADMIN = "/isExistsCpPortalAdmin";

    public static final String DASHBOARD_URI_PARAMS = "?sessionRefresh=true";

    public static final List<String> KEYCLOAK_CREATE_UESR_STATUS_CODE =
            Arrays.asList(new String[]{ CommonStatusCode.CREATED.getCode(), CommonStatusCode.CONFLICT.getCode() });

    public static final String USER_SSO_ACCOUNT_CREATE_FAILED_MESSAGE =  "Failed to register Single Sign-On user account.";
}
