package com.portal.conecta.logging;

import org.springframework.web.server.ServerWebExchange;

/**
 * Resolver padrão para aplicações WebFlux que não possuem conceito explícito de rota.
 */
public class DefaultReactiveRouteResolver implements ReactiveRouteResolver {

    @Override
    public String resolve(ServerWebExchange exchange) {
        return "unmatched";
    }
}
