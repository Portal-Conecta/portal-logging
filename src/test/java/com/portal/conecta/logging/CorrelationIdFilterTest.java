package com.portal.conecta.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorrelationIdFilterTest {

    private static final String HEADER_NAME = "X-Correlation-Id";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LoggingProperties properties = new LoggingProperties("X-Correlation-Id", 128, true);
        CorrelationIdResolver resolver = new CorrelationIdResolver();
        CorrelationIdFilter filter = new CorrelationIdFilter(resolver, properties);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .addFilter(filter)
                .build();
    }

    @Test
    @DisplayName("deve preservar X-Correlation-Id válido na response")
    void shouldPreserveValidCorrelationId() throws Exception {
        MvcResult result = mockMvc.perform(get("/stub").header(HEADER_NAME, "hub-test-123"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader(HEADER_NAME)).isEqualTo("hub-test-123");
    }

    @Test
    @DisplayName("deve gerar UUID na response quando header está ausente")
    void shouldGenerateUuidWhenHeaderIsAbsent() throws Exception {
        MvcResult result = mockMvc.perform(get("/stub"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader(HEADER_NAME))
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID na response quando header está vazio")
    void shouldGenerateUuidWhenHeaderIsBlank() throws Exception {
        MvcResult result = mockMvc.perform(get("/stub").header(HEADER_NAME, ""))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader(HEADER_NAME))
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header tem caractere inválido")
    void shouldGenerateUuidWhenHeaderIsInvalid() throws Exception {
        MvcResult result = mockMvc.perform(get("/stub").header(HEADER_NAME, "invalid id!"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader(HEADER_NAME))
                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve sempre devolver X-Correlation-Id na response")
    void shouldAlwaysReturnCorrelationIdInResponse() throws Exception {
        MvcResult result = mockMvc.perform(get("/stub"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader(HEADER_NAME)).isNotBlank();
    }

    @Test
    @DisplayName("deve limpar o MDC ao final da requisição")
    void shouldClearMdcAfterRequest() throws Exception {
        mockMvc.perform(get("/stub").header(HEADER_NAME, "hub-test-123"))
                .andExpect(status().isOk());

        assertThat(MDC.get(LoggingContextKeys.CORRELATION_ID)).isNull();
    }

    @RestController
    static class StubController {
        @GetMapping("/stub")
        ResponseEntity<Void> stub() {
            return ResponseEntity.ok().build();
        }
    }
}