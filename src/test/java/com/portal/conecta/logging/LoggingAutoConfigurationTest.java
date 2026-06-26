package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(LoggingAutoConfiguration.class);

    @Test
    @DisplayName("deve registrar CorrelationIdFilter no contexto")
    void shouldRegisterCorrelationIdFilter() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(CorrelationIdFilter.class));
    }

    @Test
    @DisplayName("deve registrar AccessLogFilter no contexto")
    void shouldRegisterAccessLogFilter() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(AccessLogFilter.class));
    }

    @Test
    @DisplayName("deve registrar UserIdResolver padrão quando não há implementação customizada")
    void shouldRegisterDefaultUserIdResolver() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(UserIdResolver.class));
    }

    @Test
    @DisplayName("deve usar UserIdResolver customizado quando fornecido pelo serviço")
    void shouldUseCustomUserIdResolverWhenProvided() {
        contextRunner
                .withBean(UserIdResolver.class, () -> () -> java.util.Optional.of("custom-user"))
                .run(context -> {
                    assertThat(context).hasSingleBean(UserIdResolver.class);
                    assertThat(context.getBean(UserIdResolver.class).resolve())
                            .contains("custom-user");
                });
    }
}