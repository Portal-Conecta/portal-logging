package com.portal.conecta.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(
        prefix = "portal.observability.logging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdResolver correlationIdResolver() {
        return new CorrelationIdResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserIdResolver userIdResolver() {
        return new SecurityUserIdResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdFilter correlationIdFilter(
            CorrelationIdResolver correlationIdResolver,
            LoggingProperties loggingProperties
    ) {
        return new CorrelationIdFilter(correlationIdResolver, loggingProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessLogFilter accessLogFilter(
            UserIdResolver userIdResolver,
            LoggingProperties loggingProperties
    ) {
        return new AccessLogFilter(userIdResolver, loggingProperties);
    }

}