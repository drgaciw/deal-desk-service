package com.aciworldwide.dealdesk.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void validCorrelationId_isPreservedInResponseAndMdc() throws Exception {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", correlationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        assertThat(response.getHeader("X-Correlation-ID")).isEqualTo(correlationId);
        assertThat(mdcValue.get()).isEqualTo(correlationId);
    }

    @Test
    void blankCorrelationId_generatesUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).isNotBlank();
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void missingCorrelationId_generatesUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).isNotBlank();
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void crlfInjection_isRejectedAndReplaced() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", "safe\r\nforged");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).doesNotContain("\r", "\n", "forged");
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void oversizedCorrelationId_isRejectedAndReplaced() throws Exception {
        String oversized = "a".repeat(65);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", oversized);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).isNotEqualTo(oversized);
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    private FilterChain capturingChain(AtomicReference<String> mdcValue) {
        return (request, response) -> mdcValue.set(MDC.get("correlationId"));
    }
}
