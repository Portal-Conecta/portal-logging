package com.portal.conecta.logging;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Emite access log estruturado em aplicações WebFlux.
 */
public class ReactiveAccessLogWebFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ReactiveAccessLogWebFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final LoggingProperties loggingProperties;
    private final ReactiveRouteResolver routeResolver;
    @Nullable
    private final Tracer tracer;

    public ReactiveAccessLogWebFilter(
            LoggingProperties loggingProperties,
            ReactiveRouteResolver routeResolver
    ) {
        this(loggingProperties, routeResolver, null);
    }

    public ReactiveAccessLogWebFilter(
            LoggingProperties loggingProperties,
            ReactiveRouteResolver routeResolver,
            @Nullable Tracer tracer
    ) {
        this.loggingProperties = loggingProperties;
        this.routeResolver = routeResolver;
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startedAt = System.currentTimeMillis();
        AtomicBoolean logged = new AtomicBoolean(false);

        return chain.filter(exchange)
                .doOnEach(signal -> {
                    if ((signal.isOnComplete() || signal.isOnError()) && logged.compareAndSet(false, true)) {
                        logRequest(exchange, startedAt, signal.getContextView());
                    }
                })
                .doFinally(signalType -> {
                    if (logged.compareAndSet(false, true)) {
                        logRequest(exchange, startedAt, Context.empty());
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void logRequest(ServerWebExchange exchange, long startedAt, ContextView contextView) {
        if (!loggingProperties.isAccessLogEnabled()) {
            return;
        }

        long durationMs = System.currentTimeMillis() - startedAt;
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        int status = statusCode != null ? statusCode.value() : 0;
        String path = exchange.getRequest().getPath().value();

        if (shouldIgnore(path, status)) {
            return;
        }

        String correlationId = exchange.getAttributeOrDefault(
                ReactiveCorrelationIdWebFilter.CORRELATION_ID_ATTRIBUTE,
                "unknown"
        );
        String route = routeResolver.resolve(exchange);

        try (ContextSnapshot.Scope ignored = ContextSnapshot.setAllThreadLocalsFrom(contextView)) {
            MDC.put(LoggingContextKeys.CORRELATION_ID, correlationId);
            MDC.put(LoggingContextKeys.METHOD, String.valueOf(exchange.getRequest().getMethod()));
            MDC.put(LoggingContextKeys.PATH, path);
            MDC.put(LoggingContextKeys.ROUTE, route);
            MDC.put(LoggingContextKeys.STATUS, String.valueOf(status));
            MDC.put(LoggingContextKeys.DURATION_MS, String.valueOf(durationMs));
            putTraceContext();

            log.info("Requisicao HTTP concluida.");
        } finally {
            MDC.remove(LoggingContextKeys.CORRELATION_ID);
            MDC.remove(LoggingContextKeys.METHOD);
            MDC.remove(LoggingContextKeys.PATH);
            MDC.remove(LoggingContextKeys.ROUTE);
            MDC.remove(LoggingContextKeys.STATUS);
            MDC.remove(LoggingContextKeys.DURATION_MS);
            MDC.remove(LoggingContextKeys.TRACE_ID);
            MDC.remove(LoggingContextKeys.SPAN_ID);
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
}
