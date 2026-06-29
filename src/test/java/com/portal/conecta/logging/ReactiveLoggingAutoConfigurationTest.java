package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveLoggingAutoConfigurationTest {

    private final ReactiveWebApplicationContextRunner contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(ReactiveLoggingAutoConfiguration.class);

    @Test
    @DisplayName("deve registrar filtros WebFlux no contexto reativo")
    void shouldRegisterReactiveFilters() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ReactiveCorrelationIdWebFilter.class);
            assertThat(context).hasSingleBean(ReactiveAccessLogWebFilter.class);
        });
    }

    @Test
    @DisplayName("deve permitir resolver rota customizada no access log reativo")
    void shouldUseCustomRouteResolver() {
        contextRunner
                .withBean(ReactiveRouteResolver.class, () -> exchange -> "custom-route")
                .run(context -> assertThat(context.getBean(ReactiveRouteResolver.class).resolve(null))
                        .isEqualTo("custom-route"));
    }
}
