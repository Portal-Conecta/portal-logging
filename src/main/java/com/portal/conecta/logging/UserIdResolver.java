package com.portal.conecta.logging;

import java.util.Optional;

public interface UserIdResolver {
    Optional<String> resolve();
}