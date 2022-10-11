package org.openpaas.servicebroker.container.platform.common;

import org.openpaas.servicebroker.container.platform.model.Constants;
import org.springframework.util.StringUtils;

public class CommonUtils {
    /**
     * LOGGER 개행문자 제거 (Object)
     *
     * @param obj
     * @return String the replaced string
     */
    public static String loggerReplace(Object obj) {
        return obj.toString().replaceAll("[\r\n]","");
    }

    /**
     * LOGGER 개행문자 제거 (String)
     *
     * @param str
     * @return String the replaced string
     */
    public static String loggerReplace(String str) {
        return str.replaceAll("[\r\n]","");
    }


    /**
     * Proc replace null value string
     *
     * @param requestString the request string
     * @return the string
     */
    public static String procReplaceNullValue(String requestString) {
        return (StringUtils.isEmpty(requestString)) ? Constants.NULL_REPLACE_TEXT : requestString;
    }

}
