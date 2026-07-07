package com.portal.conecta.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propriedades de configuração do subsistema de logging do Portal Conecta.
 *
 * <p>Prefixo: {@code portal.observability.logging}.
 *
 * <ul>
 *   <li>{@code correlation-header} — nome do header HTTP para o correlation ID
 *       (padrão: {@code X-Correlation-Id});</li>
 *   <li>{@code max-correlation-id-length} — comprimento máximo aceito para o valor
 *       do header antes de gerar um UUID novo (padrão: {@code 128});</li>
 *   <li>{@code access-log-enabled} — habilita ou desabilita o {@link AccessLogFilter}
 *       (padrão: {@code true}).</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "portal.observability.logging")
public class LoggingProperties {

    private String correlationHeader = "X-Correlation-Id";
    private int maxCorrelationIdLength = 128;
    private boolean accessLogEnabled = true;
    private List<String> accessLogIgnoredPaths = new ArrayList<>(
            List.of("/actuator/health", "/actuator/health/**", "/actuator/prometheus")
    );

    public LoggingProperties() {
    }

    public LoggingProperties(String correlationHeader, int maxCorrelationIdLength, boolean accessLogEnabled) {
        this.correlationHeader = correlationHeader;
        this.maxCorrelationIdLength = maxCorrelationIdLength;
        this.accessLogEnabled = accessLogEnabled;
    }

    public String getCorrelationHeader() {
        return correlationHeader;
    }

    public void setCorrelationHeader(String correlationHeader) {
        this.correlationHeader = correlationHeader;
    }

    public int getMaxCorrelationIdLength() {
        return maxCorrelationIdLength;
    }

    public void setMaxCorrelationIdLength(int maxCorrelationIdLength) {
        this.maxCorrelationIdLength = maxCorrelationIdLength;
    }

    public boolean isAccessLogEnabled() {
        return accessLogEnabled;
    }

    public void setAccessLogEnabled(boolean accessLogEnabled) {
        this.accessLogEnabled = accessLogEnabled;
    }

    public List<String> getAccessLogIgnoredPaths() {
        return accessLogIgnoredPaths;
    }

    public void setAccessLogIgnoredPaths(List<String> accessLogIgnoredPaths) {
        this.accessLogIgnoredPaths = accessLogIgnoredPaths != null ? accessLogIgnoredPaths : new ArrayList<>();
    }

}
