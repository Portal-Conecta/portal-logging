package com.portal.conecta.logging;

import org.springframework.web.server.ServerWebExchange;

/**
 * Resolve o identificador da rota em aplicações WebFlux.
 */
@FunctionalInterface
public interface ReactiveRouteResolver {

    /**
     * Resolve a rota associada à troca HTTP atual.
     *
     * @param exchange contexto WebFlux atual
     * @return identificador da rota ou {@code unmatched}
     */
    String resolve(ServerWebExchange exchange);
}
