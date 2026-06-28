package com.portal.conecta.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdProviderTest {

    private final CorrelationIdProvider provider = new CorrelationIdProvider();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("deve retornar o correlationId do MDC quando presente")
    void shouldReturnCorrelationIdFromMdc() {
        MDC.put(LoggingContextKeys.CORRELATION_ID, "hub-test-123");

        String result = provider.get();

        assertThat(result).isEqualTo("hub-test-123");
    }

    @Test
    @DisplayName("deve gerar UUID quando MDC não tem correlationId")
    void shouldGenerateUuidWhenMdcIsEmpty() {
        String result = provider.get();

        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando correlationId no MDC está em branco")
    void shouldGenerateUuidWhenMdcValueIsBlank() {
        MDC.put(LoggingContextKeys.CORRELATION_ID, "   ");

        String result = provider.get();

        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }
}