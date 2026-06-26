package com.portal.conecta.logging;

import org.slf4j.MDC;

import java.util.UUID;

public class CorrelationIdProvider {

    public String get() {
        String correlationId = MDC.get(LoggingContextKeys.CORRELATION_ID);
        return (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();
    }

}