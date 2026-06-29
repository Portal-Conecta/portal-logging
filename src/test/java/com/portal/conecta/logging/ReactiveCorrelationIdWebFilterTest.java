package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveCorrelationIdWebFilterTest {

    private static final String HEADER_NAME = "X-Correlation-Id";

    private final WebTestClient webTestClient = WebTestClient
            .bindToController(new StubController())
            .webFilter(new ReactiveCorrelationIdWebFilter(
                    new CorrelationIdResolver(),
                    new LoggingProperties("X-Correlation-Id", 128, true)
            ))
            .build();

    @Test
    @DisplayName("deve preservar X-Correlation-Id valido no fluxo WebFlux")
    void shouldPreserveValidCorrelationId() {
        webTestClient.get()
                .uri("/stub")
                .header(HEADER_NAME, "gateway-test-123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HEADER_NAME, "gateway-test-123")
                .expectBody(String.class)
                .value(body -> assertThat(body).isEqualTo("gateway-test-123"));
    }

    @Test
    @DisplayName("deve gerar UUID quando X-Correlation-Id esta ausente no fluxo WebFlux")
    void shouldGenerateUuidWhenHeaderIsAbsent() {
        webTestClient.get()
                .uri("/stub")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HEADER_NAME, "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    @DisplayName("deve gerar UUID quando X-Correlation-Id e invalido no fluxo WebFlux")
    void shouldGenerateUuidWhenHeaderIsInvalid() {
        webTestClient.get()
                .uri("/stub")
                .header(HEADER_NAME, "invalid id!")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HEADER_NAME, "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @RestController
    static class StubController {

        @GetMapping("/stub")
        ResponseEntity<String> stub(@org.springframework.web.bind.annotation.RequestHeader HttpHeaders headers) {
            return ResponseEntity.ok(headers.getFirst(HEADER_NAME));
        }
    }
}
