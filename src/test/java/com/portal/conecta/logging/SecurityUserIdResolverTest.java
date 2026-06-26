package com.portal.conecta.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUserIdResolverTest {

    private final SecurityUserIdResolver resolver = new SecurityUserIdResolver();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("deve retornar vazio quando não há autenticação")
    void shouldReturnEmptyWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        Optional<String> result = resolver.resolve();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve retornar vazio quando autenticação é anônima")
    void shouldReturnEmptyWhenAnonymous() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        SecurityContextHolder.getContext().setAuthentication(anonymous);

        Optional<String> result = resolver.resolve();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve retornar o nome do usuário autenticado")
    void shouldReturnUsernameWhenAuthenticated() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-uuid-123",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> result = resolver.resolve();

        assertThat(result).contains("user-uuid-123");
    }

    @Test
    @DisplayName("deve retornar vazio quando o nome do usuário está em branco")
    void shouldReturnEmptyWhenUsernameIsBlank() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "   ",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<String> result = resolver.resolve();

        assertThat(result).isEmpty();
    }
}