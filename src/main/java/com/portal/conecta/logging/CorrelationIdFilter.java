package com.portal.conecta.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(-100)
@RequiredArgsConstructor
public class CorrelationIdFilter extends OncePerRequestFilter {

    private final CorrelationIdResolver correlationIdResolver;
    private final LoggingProperties loggingProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String correlationId = correlationIdResolver.resolve(
                request,
                loggingProperties.getCorrelationHeader(),
                loggingProperties.getMaxCorrelationIdLength()
        );

        MDC.put(LoggingContextKeys.CORRELATION_ID, correlationId);
        response.setHeader(loggingProperties.getCorrelationHeader(), correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(LoggingContextKeys.CORRELATION_ID);
        }
    }

}