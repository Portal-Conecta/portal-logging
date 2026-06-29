package com.portal.conecta.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Emite access log estruturado em aplicações WebFlux.
 */
public class ReactiveAccessLogWebFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ReactiveAccessLogWebFilter.class);

    private final LoggingProperties loggingProperties;
    private final ReactiveRouteResolver routeResolver;

    public ReactiveAccessLogWebFilter(
            LoggingProperties loggingProperties,
            ReactiveRouteResolver routeResolver
    ) {
        this.loggingProperties = loggingProperties;
        this.routeResolver = routeResolver;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startedAt = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signalType -> logRequest(exchange, startedAt));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private void logRequest(ServerWebExchange exchange, long startedAt) {
        if (!loggingProperties.isAccessLogEnabled()) {
            return;
        }

        long durationMs = System.currentTimeMillis() - startedAt;
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        String correlationId = exchange.getAttributeOrDefault(
                ReactiveCorrelationIdWebFilter.CORRELATION_ID_ATTRIBUTE,
                "unknown"
        );

        log.info(
                "http_request correlationId={} method={} path={} route={} status={} durationMs={}",
                correlationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath().value(),
                routeResolver.resolve(exchange),
                statusCode != null ? statusCode.value() : 0,
                durationMs
        );
    }
}
