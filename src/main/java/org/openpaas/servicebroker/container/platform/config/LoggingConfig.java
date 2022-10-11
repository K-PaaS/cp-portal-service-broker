package org.openpaas.servicebroker.container.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Logging을 위한 설정 클래스
 *
 * @author Hyerin
 * @since 2018.07.24
 * @version 20180724
 */
@Configuration
public class LoggingConfig {

    public LoggingConfig () { }

    @Bean
    public void consoleAppender () {
    }

}
