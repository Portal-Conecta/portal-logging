package com.portal.conecta.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccessLogFilterTest {

    @Mock
    private UserIdResolver userIdResolver;

    class CapturingAccessLogFilter extends AccessLogFilter {
        final Map<String, String> captured = new HashMap<>();

        CapturingAccessLogFilter() {
            super(userIdResolver, new LoggingProperties("X-Correlation-Id", 128, true));
        }

        @Override
        protected void afterLog() {
            captured.put(LoggingContextKeys.METHOD,      MDC.get(LoggingContextKeys.METHOD));
            captured.put(LoggingContextKeys.PATH,        MDC.get(LoggingContextKeys.PATH));
            captured.put(LoggingContextKeys.STATUS,      MDC.get(LoggingContextKeys.STATUS));
            captured.put(LoggingContextKeys.DURATION_MS, MDC.get(LoggingContextKeys.DURATION_MS));
            captured.put(LoggingContextKeys.USER_ID,     MDC.get(LoggingContextKeys.USER_ID));
        }
    }

    private CapturingAccessLogFilter filter;

    private MockMvc buildMockMvc() {
        filter = new CapturingAccessLogFilter();
        return MockMvcBuilders
                .standaloneSetup(new StubController())
                .addFilter(filter)
                .build();
    }

    @Test
    @DisplayName("deve popular MDC com method ao executar a requisição")
    void shouldPopulateMdcWithMethod() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.METHOD)).isEqualTo("GET");
    }

    @Test
    @DisplayName("deve popular MDC com path sem query string")
    void shouldPopulateMdcWithPathWithoutQueryString() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub?token=secret&foo=bar")).andExpect(status().isOk());

        assertThat(filter.captured.get(LoggingContextKeys.PATH)).isEqualTo("/stub");
        assertThat(filter.captured.get(LoggingContextKeys.PATH)).doesNotContain("token");
        assertThat(filter.captured.get(LoggingContextKeys.PATH)).doesNotContain("secret");
    }

    @Test
    @DisplayName("deve popular MDC com status da resposta")
    void shouldPopulateMdcWithStatus() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.STATUS)).isEqualTo("200");
    }

    @Test
    @DisplayName("deve popular MDC com durationMs")
    void shouldPopulateMdcWithDurationMs() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());

        assertThat(filter.captured.get(LoggingContextKeys.DURATION_MS)).isNotNull();
        assertThat(Long.parseLong(filter.captured.get(LoggingContextKeys.DURATION_MS))).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("deve omitir userId quando requisição é anônima")
    void shouldOmitUserIdWhenAnonymous() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("deve incluir userId quando usuário está autenticado")
    void shouldIncludeUserIdWhenAuthenticated() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.of("user-uuid-123"));
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());
        assertThat(filter.captured.get(LoggingContextKeys.USER_ID)).isEqualTo("user-uuid-123");
    }

    @Test
    @DisplayName("deve limpar MDC ao final da requisição")
    void shouldClearMdcAfterRequest() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/stub")).andExpect(status().isOk());

        assertThat(MDC.get(LoggingContextKeys.METHOD)).isNull();
        assertThat(MDC.get(LoggingContextKeys.PATH)).isNull();
        assertThat(MDC.get(LoggingContextKeys.STATUS)).isNull();
        assertThat(MDC.get(LoggingContextKeys.DURATION_MS)).isNull();
        assertThat(MDC.get(LoggingContextKeys.USER_ID)).isNull();
    }

    @Test
    @DisplayName("deve omitir Authorization do MDC")
    void shouldNotLogAuthorizationHeader() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc()
                .perform(get("/stub").header("Authorization", "Bearer secret-token"))
                .andExpect(status().isOk());

        assertThat(filter.captured.get("Authorization")).isNull();
        assertThat(MDC.get("Authorization")).isNull();
    }

    @Test
    @DisplayName("deve ignorar requisição quando accessLogEnabled é false")
    void shouldSkipFilterWhenAccessLogDisabled() throws Exception {
        UserIdResolver resolver = Mockito.mock(UserIdResolver.class);
        AccessLogFilter disabledFilter = new AccessLogFilter(
                resolver,
                new LoggingProperties("X-Correlation-Id", 128, false)
        );

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .addFilter(disabledFilter)
                .build();

        mvc.perform(get("/stub")).andExpect(status().isOk());

        Mockito.verify(resolver, Mockito.never()).resolve();
    }

    @Test
    @DisplayName("deve ignorar access log de healthcheck com sucesso")
    void shouldIgnoreSuccessfulHealthcheckAccessLog() throws Exception {
        buildMockMvc().perform(get("/actuator/health/readiness")).andExpect(status().isOk());

        assertThat(filter.captured).isEmpty();
        Mockito.verify(userIdResolver, Mockito.never()).resolve();
    }

    @Test
    @DisplayName("deve ignorar access log de prometheus com sucesso")
    void shouldIgnoreSuccessfulPrometheusAccessLog() throws Exception {
        buildMockMvc().perform(get("/actuator/prometheus")).andExpect(status().isOk());

        assertThat(filter.captured).isEmpty();
        Mockito.verify(userIdResolver, Mockito.never()).resolve();
    }

    @Test
    @DisplayName("deve registrar access log de actuator quando status for erro")
    void shouldLogActuatorAccessLogWhenStatusIsError() throws Exception {
        Mockito.when(userIdResolver.resolve()).thenReturn(Optional.empty());
        buildMockMvc().perform(get("/actuator/health/failing")).andExpect(status().isInternalServerError());

        assertThat(filter.captured.get(LoggingContextKeys.PATH)).isEqualTo("/actuator/health/failing");
        assertThat(filter.captured.get(LoggingContextKeys.STATUS)).isEqualTo("500");
    }

    @RestController
    static class StubController {
        @GetMapping("/stub")
        ResponseEntity<Void> stub() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/actuator/health/readiness")
        ResponseEntity<Void> health() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/actuator/prometheus")
        ResponseEntity<Void> prometheus() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/actuator/health/failing")
        ResponseEntity<Void> failingHealth() {
            return ResponseEntity.internalServerError().build();
        }
    }
}
