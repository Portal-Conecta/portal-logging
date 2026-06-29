package com.portal.conecta.logging;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Normaliza e propaga correlation ID em aplicações WebFlux.
 */
public class ReactiveCorrelationIdWebFilter implements WebFilter, Ordered {

    public static final String CORRELATION_ID_ATTRIBUTE = LoggingContextKeys.CORRELATION_ID;

    private final CorrelationIdResolver correlationIdResolver;
    private final LoggingProperties loggingProperties;

    public ReactiveCorrelationIdWebFilter(
            CorrelationIdResolver correlationIdResolver,
            LoggingProperties loggingProperties
    ) {
        this.correlationIdResolver = correlationIdResolver;
        this.loggingProperties = loggingProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = loggingProperties.getCorrelationHeader();
        String correlationId = correlationIdResolver.resolve(
                exchange.getRequest().getHeaders().getFirst(header),
                header,
                loggingProperties.getMaxCorrelationIdLength()
        );

        exchange.getAttributes().put(CORRELATION_ID_ATTRIBUTE, correlationId);
        exchange.getResponse().getHeaders().set(header, correlationId);

        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.set(header, correlationId))
                .build();

        return chain.filter(exchange.mutate().request(request).build())
                .contextWrite(context -> context.put(CORRELATION_ID_ATTRIBUTE, correlationId))
                .doFinally(signalType -> MDC.remove(LoggingContextKeys.CORRELATION_ID));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
