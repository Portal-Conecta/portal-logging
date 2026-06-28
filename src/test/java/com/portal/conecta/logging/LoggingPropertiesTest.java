package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingPropertiesTest {

    @Test
    @DisplayName("deve retornar valores default quando construído sem argumentos")
    void shouldReturnDefaultValues() {
        LoggingProperties properties = new LoggingProperties();

        assertThat(properties.getCorrelationHeader()).isEqualTo("X-Correlation-Id");
        assertThat(properties.getMaxCorrelationIdLength()).isEqualTo(128);
        assertThat(properties.isAccessLogEnabled()).isTrue();
    }

    @Test
    @DisplayName("deve atualizar correlationHeader via setter")
    void shouldUpdateCorrelationHeaderViaSetter() {
        LoggingProperties properties = new LoggingProperties();
        properties.setCorrelationHeader("X-Request-Id");

        assertThat(properties.getCorrelationHeader()).isEqualTo("X-Request-Id");
    }

    @Test
    @DisplayName("deve atualizar maxCorrelationIdLength via setter")
    void shouldUpdateMaxCorrelationIdLengthViaSetter() {
        LoggingProperties properties = new LoggingProperties();
        properties.setMaxCorrelationIdLength(64);

        assertThat(properties.getMaxCorrelationIdLength()).isEqualTo(64);
    }

    @Test
    @DisplayName("deve atualizar accessLogEnabled via setter")
    void shouldUpdateAccessLogEnabledViaSetter() {
        LoggingProperties properties = new LoggingProperties();
        properties.setAccessLogEnabled(false);

        assertThat(properties.isAccessLogEnabled()).isFalse();
    }

    @Test
    @DisplayName("deve respeitar valores passados pelo construtor com argumentos")
    void shouldRespectValuesFromConstructor() {
        LoggingProperties properties = new LoggingProperties("X-Request-Id", 64, false);

        assertThat(properties.getCorrelationHeader()).isEqualTo("X-Request-Id");
        assertThat(properties.getMaxCorrelationIdLength()).isEqualTo(64);
        assertThat(properties.isAccessLogEnabled()).isFalse();
    }
}