package com.portal.conecta.logging;

import java.util.UUID;
import java.util.regex.Pattern;

public class CorrelationIdResolver {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");

    /**
     * Resolve o correlation ID a partir de um valor de header ja extraido.
     *
     * @param candidate  valor recebido no header.
     * @param headerName nome do header configurado.
     * @param maxLength  comprimento maximo aceito para o valor do header.
     * @return valor do header se valido, ou um UUID gerado automaticamente.
     */
    public String resolve(String candidate, String headerName, int maxLength) {
        if (headerName == null || headerName.isBlank()) {
            return UUID.randomUUID().toString();
        }

        if (isValid(candidate, maxLength)) {
            return candidate;
        }

        return UUID.randomUUID().toString();
    }

    private boolean isValid(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return false;
        }

        if (value.length() > maxLength) {
            return false;
        }

        return VALID_PATTERN.matcher(value).matches();
    }
}
