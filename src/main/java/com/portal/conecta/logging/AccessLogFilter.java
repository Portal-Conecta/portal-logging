package com.portal.conecta.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de access log que registra método, path, status e duração de cada requisição HTTP.
 *
 * <p>Executado com {@code @Order(-90)}, após {@link CorrelationIdFilter}.
 * Popula o MDC com as chaves definidas em {@link LoggingContextKeys} e as remove ao final,
 * garantindo que não vazem entre requisições.
 *
 * <p>Inclui o {@code userId} do usuário autenticado quando disponível no contexto de segurança.
 * Pode ser desativado via {@code portal.observability.logging.access-log-enabled=false}.
 *
 * <p>O método {@link #afterLog()} é um hook vazio destinado a extensão em testes.
 */
@Order(-90)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final UserIdResolver userIdResolver;
    private final LoggingProperties loggingProperties;
    @Nullable
    private final Tracer tracer;

    public AccessLogFilter(UserIdResolver userIdResolver, LoggingProperties loggingProperties) {
        this(userIdResolver, loggingProperties, null);
    }

    public AccessLogFilter(
            UserIdResolver userIdResolver,
            LoggingProperties loggingProperties,
            @Nullable Tracer tracer
    ) {
        this.userIdResolver = userIdResolver;
        this.loggingProperties = loggingProperties;
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!loggingProperties.isAccessLogEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            String path = request.getRequestURI();
            int status = response.getStatus();

            if (!shouldIgnore(path, status)) {
                MDC.put(LoggingContextKeys.METHOD, request.getMethod());
                MDC.put(LoggingContextKeys.PATH, path);
                MDC.put(LoggingContextKeys.STATUS, String.valueOf(status));
                MDC.put(LoggingContextKeys.DURATION_MS, String.valueOf(durationMs));

                userIdResolver.resolve()
                        .ifPresent(userId -> MDC.put(LoggingContextKeys.USER_ID, userId));
                putTraceContext();

                log.info("Requisicao HTTP concluida.");

                afterLog();

                MDC.remove(LoggingContextKeys.METHOD);
                MDC.remove(LoggingContextKeys.PATH);
                MDC.remove(LoggingContextKeys.STATUS);
                MDC.remove(LoggingContextKeys.DURATION_MS);
                MDC.remove(LoggingContextKeys.USER_ID);
                MDC.remove(LoggingContextKeys.TRACE_ID);
                MDC.remove(LoggingContextKeys.SPAN_ID);
            }
        }
    }

    private boolean shouldIgnore(String path, int status) {
        if (status >= 400) {
            return false;
        }

        return loggingProperties.getAccessLogIgnoredPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private void putTraceContext() {
        if (tracer == null) {
            return;
        }

        Span span = tracer.currentSpan();
        if (span == null) {
            return;
        }

        MDC.put(LoggingContextKeys.TRACE_ID, span.context().traceId());
        MDC.put(LoggingContextKeys.SPAN_ID, span.context().spanId());
    }

    protected void afterLog() {
    }

}
