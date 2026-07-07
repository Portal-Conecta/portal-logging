package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveAccessLogWebFilterTest {

    @Test
    @DisplayName("deve executar access log reativo sem bloquear a cadeia WebFlux")
    void shouldRunReactiveAccessLogWithoutBlockingChain() {
        WebTestClient webTestClient = WebTestClient
                .bindToController(new StubController())
                .webFilter(new ReactiveCorrelationIdWebFilter(
                        new CorrelationIdResolver(),
                        new LoggingProperties("X-Correlation-Id", 128, true)
                ))
                .webFilter(new ReactiveAccessLogWebFilter(
                        new LoggingProperties("X-Correlation-Id", 128, true),
                        exchange -> "stub-route"
                ))
                .build();

        webTestClient.get()
                .uri("/stub")
                .header("X-Correlation-Id", "gateway-test-123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Correlation-Id", "gateway-test-123");
    }

    @Test
    @DisplayName("deve ignorar access log reativo de healthcheck com sucesso")
    void shouldIgnoreSuccessfulReactiveHealthcheckAccessLog() {
        AtomicInteger routeResolutions = new AtomicInteger();

        WebTestClient webTestClient = WebTestClient
                .bindToController(new StubController())
                .webFilter(new ReactiveAccessLogWebFilter(
                        new LoggingProperties("X-Correlation-Id", 128, true),
                        exchange -> {
                            routeResolutions.incrementAndGet();
                            return "stub-route";
                        }
                ))
                .build();

        webTestClient.get()
                .uri("/actuator/health/readiness")
                .exchange()
                .expectStatus().isOk();

        assertThat(routeResolutions).hasValue(0);
    }

    @Test
    @DisplayName("deve registrar access log reativo de actuator quando status for erro")
    void shouldLogReactiveActuatorAccessLogWhenStatusIsError() {
        AtomicInteger routeResolutions = new AtomicInteger();

        WebTestClient webTestClient = WebTestClient
                .bindToController(new StubController())
                .webFilter(new ReactiveAccessLogWebFilter(
                        new LoggingProperties("X-Correlation-Id", 128, true),
                        exchange -> {
                            routeResolutions.incrementAndGet();
                            return "stub-route";
                        }
                ))
                .build();

        webTestClient.get()
                .uri("/actuator/health/failing")
                .exchange()
                .expectStatus().is5xxServerError();

        assertThat(routeResolutions).hasValue(1);
    }

    @RestController
    static class StubController {

        @GetMapping("/stub")
        ResponseEntity<Void> stub() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/actuator/health/readiness")
        ResponseEntity<Void> health() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/actuator/health/failing")
        ResponseEntity<Void> failingHealth() {
            return ResponseEntity.internalServerError().build();
        }
    }
}
