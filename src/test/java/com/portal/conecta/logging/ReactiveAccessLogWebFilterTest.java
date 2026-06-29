package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RestController
    static class StubController {

        @GetMapping("/stub")
        ResponseEntity<Void> stub() {
            return ResponseEntity.ok().build();
        }
    }
}
