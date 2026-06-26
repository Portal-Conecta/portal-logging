package com.portal.conecta.logging;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementação padrão de {@link UserIdResolver} baseada no Spring Security.
 *
 * <p>Retorna {@code authentication.getName()} quando o usuário está autenticado
 * e não é anônimo. O serviço consumidor pode substituir esse bean registrando
 * sua própria implementação de {@link UserIdResolver}.</p>
 */
public class SecurityUserIdResolver implements UserIdResolver {

    @Override
    public Optional<String> resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String name = authentication.getName();
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(name);
    }

}