package com.weblog.springbootstarter.config;

import com.weblog.springbootstarter.aop.WebLogAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User: sunling
 * Date: 2024/7/23 14:00
 * Description:
 **/
@Configuration
@EnableConfigurationProperties({WebLogProperties.class})
@ConditionalOnClass(WebLogAspect.class)
//@ConditionalOnProperty(
//        prefix = "weblog",
//        value = "disable",
//        havingValue = "true"
//)
public class WebLogAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public WebLogAspect webLogAspect() {
        return new WebLogAspect();
    }
}