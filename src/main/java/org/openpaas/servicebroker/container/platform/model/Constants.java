package org.openpaas.servicebroker.container.platform.model;

import org.openpaas.servicebroker.container.platform.common.CommonStatusCode;

import java.util.Arrays;
import java.util.List;

public class Constants {
    
    public static final String TOKEN_KEY = "caas_admin";
    public static final String RESULT_STATUS_SUCCESS = "SUCCESS";
    public static final String RESULT_STATUS_FAIL = "FAIL";

    public static final String NULL_REPLACE_TEXT = "-";

    public static final String URI_CP_COMMON_API_CHECK_EXISTS_ADMIN = "/isExistsCpPortalAdmin";

    public static final List<String> KEYCLOAK_CREATE_UESR_STATUS_CODE =
            Arrays.asList(new String[]{ CommonStatusCode.CREATED.getCode(), CommonStatusCode.CONFLICT.getCode() });

    public static final String USER_SSO_ACCOUNT_CREATE_FAILED_MESSAGE =  "Failed to register Single Sign-On user account.";
}
