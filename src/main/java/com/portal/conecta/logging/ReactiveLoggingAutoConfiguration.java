package com.portal.conecta.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

/**
 * Autoconfiguration reativa da biblioteca {@code portal-logging}.
 */
@AutoConfiguration
@ConditionalOnClass(WebFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(
        prefix = "portal.observability.logging",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@EnableConfigurationProperties(LoggingProperties.class)
public class ReactiveLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdResolver correlationIdResolver() {
        return new CorrelationIdResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveRouteResolver reactiveRouteResolver() {
        return new DefaultReactiveRouteResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveCorrelationIdWebFilter reactiveCorrelationIdWebFilter(
            CorrelationIdResolver correlationIdResolver,
            LoggingProperties loggingProperties
    ) {
        return new ReactiveCorrelationIdWebFilter(correlationIdResolver, loggingProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveAccessLogWebFilter reactiveAccessLogWebFilter(
            LoggingProperties loggingProperties,
            ReactiveRouteResolver reactiveRouteResolver
    ) {
        return new ReactiveAccessLogWebFilter(loggingProperties, reactiveRouteResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdProvider correlationIdProvider() {
        return new CorrelationIdProvider();
    }
}
