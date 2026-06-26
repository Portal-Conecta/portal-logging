package com.portal.conecta.logging;

import java.util.Optional;

/**
 * Contrato para resolução do identificador do usuário autenticado.
 *
 * <p>A biblioteca fornece uma implementação padrão via {@link SecurityUserIdResolver},
 * baseada em {@code authentication.getName()} do Spring Security. O serviço consumidor
 * pode substituí-la registrando seu próprio bean do tipo {@link UserIdResolver}.</p>
 */
public interface UserIdResolver {

    /**
     * Resolve o identificador do usuário autenticado na requisição atual.
     *
     * @return {@link java.util.Optional} contendo o userId, ou vazio se anônimo.
     */
    Optional<String> resolve();
}