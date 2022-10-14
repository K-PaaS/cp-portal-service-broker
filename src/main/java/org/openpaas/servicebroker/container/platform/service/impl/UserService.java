package org.openpaas.servicebroker.container.platform.service.impl;
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
     * 컨테이너 플랫폼 포털 내 'SUPER-ADMIN' 권한의 관리자가 등록되어있는지 확인한다.
     *
     * @return
     **/
    public Boolean isExistsCpPortalAdmin() {
        ResultStatus resultStatus = restTemplateService.sendCpCommonApi(Constants.URI_CP_COMMON_API_CHECK_EXISTS_ADMIN, HttpMethod.GET, ResultStatus.class);
        logger.info("Check for CP Portal Admin Registration Status : [{}] {}", CommonUtils.loggerReplace(resultStatus.getResultCode()),
                CommonUtils.loggerReplace(resultStatus.getResultMessage()));

        return (resultStatus.getResultCode().equalsIgnoreCase(Constants.RESULT_STATUS_SUCCESS)) ? false : true;
    }
}