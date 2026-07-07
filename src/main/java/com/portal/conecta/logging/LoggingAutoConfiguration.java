package com.portal.conecta.logging;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

/**
 * Autoconfiguration da biblioteca {@code portal-logging}.
 *
 * <p>Registra automaticamente os filtros e beans necessários quando a biblioteca
 * está no classpath do serviço consumidor. Todo bean pode ser substituído
 * registrando uma implementação própria — o {@code @ConditionalOnMissingBean}
 * garante que a lib não sobrescreve customizações do serviço.</p>
 *
 * <p>Para desabilitar completamente:</p>
 * <pre>{@code
 * portal:
 *   observability:
 *     logging:
 *       enabled: false
 * }</pre>
 */
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
            LoggingProperties loggingProperties,
            ObjectProvider<Tracer> tracer
    ) {
        return new AccessLogFilter(userIdResolver, loggingProperties, tracer.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdProvider correlationIdProvider() {
        return new CorrelationIdProvider();
    }

}
