package com.aciworldwide.dealdesk.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcContextFilterTest {

    private final MdcContextFilter filter = new MdcContextFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    // ---------------------------------------------------------------------------
    // Correlation ID validation
    // ---------------------------------------------------------------------------

    @Test
    void validUuidCorrelationId_isPreservedInResponseAndMdc() throws Exception {
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
    void alphanumericHyphenCorrelationId_isPreserved() throws Exception {
        String correlationId = "req-abc123-XYZ";
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
        assertThat(returned).isNotBlank().isNotEqualTo("   ");
        assertThat(mdcValue.get()).isEqualTo(returned);
        // Should be a valid UUID
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
    void crlfInjection_isRejectedAndReplacedWithUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", "valid\r\nX-Injected: evil");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).doesNotContain("\r", "\n", "evil");
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void oversizedCorrelationId_isRejectedAndReplacedWithUuid() throws Exception {
        String oversized = "a".repeat(65);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", oversized);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).isNotEqualTo(oversized);
        assertThat(returned.length()).isLessThanOrEqualTo(64);
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    @Test
    void specialCharCorrelationId_isRejectedAndReplacedWithUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals");
        request.addHeader("X-Correlation-ID", "<script>alert(1)</script>");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> mdcValue = new AtomicReference<>();
        filter.doFilter(request, response, capturingChain(mdcValue));

        String returned = response.getHeader("X-Correlation-ID");
        assertThat(returned).doesNotContain("<", ">");
        assertThat(mdcValue.get()).isEqualTo(returned);
        assertThat(UUID.fromString(returned)).isNotNull();
    }

    // ---------------------------------------------------------------------------
    // Path / dealId extraction
    // ---------------------------------------------------------------------------

    @Test
    void dealId_extractedFromDealsPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/deals/deal-42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> dealId = new AtomicReference<>();
        filter.doFilter(request, response, capturingDealIdChain(dealId));

        assertThat(dealId.get()).isEqualTo("deal-42");
    }

    @Test
    void dealId_notSetForActionSegments() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/deals/submit");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> dealId = new AtomicReference<>();
        filter.doFilter(request, response, capturingDealIdChain(dealId));

        assertThat(dealId.get()).isNull();
    }

    @Test
    void requestPath_alwaysSetInMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> requestPath = new AtomicReference<>();
        filter.doFilter(request, response, capturingRequestPathChain(requestPath));

        assertThat(requestPath.get()).isEqualTo("/health");
    }

    // ---------------------------------------------------------------------------
    // Pattern constant
    // ---------------------------------------------------------------------------

    @Test
    void safePattern_acceptsMaxLength64() {
        String maxLength = "a".repeat(64);
        assertThat(MdcContextFilter.SAFE_CORRELATION_ID_PATTERN.matcher(maxLength).matches()).isTrue();
    }

    @Test
    void safePattern_rejects65Chars() {
        String tooLong = "a".repeat(65);
        assertThat(MdcContextFilter.SAFE_CORRELATION_ID_PATTERN.matcher(tooLong).matches()).isFalse();
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private FilterChain capturingChain(AtomicReference<String> mdcValue) {
        return (req, res) -> mdcValue.set(MDC.get(MdcContextFilter.MDC_CORRELATION_ID));
    }

    private FilterChain capturingDealIdChain(AtomicReference<String> dealId) {
        return (req, res) -> dealId.set(MDC.get(MdcContextFilter.MDC_DEAL_ID));
    }

    private FilterChain capturingRequestPathChain(AtomicReference<String> requestPath) {
        return (req, res) -> requestPath.set(MDC.get(MdcContextFilter.MDC_REQUEST_PATH));
    }
}
