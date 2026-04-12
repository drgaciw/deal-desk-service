package com.aciworldwide.dealdesk.filter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that populates the SLF4J MDC (Mapped Diagnostic Context) with
 * request-scoped contextual fields so every log statement within the same thread
 * automatically carries them.
 *
 * <p>Fields set:
 * <ul>
 *   <li>{@code correlationId} – taken from {@code X-Correlation-ID} request header or generated as a UUID</li>
 *   <li>{@code requestPath}   – the request URI</li>
 *   <li>{@code dealId}        – extracted from the URI when the path matches {@code /deals/{id}}</li>
 *   <li>{@code userId}        – principal name from the Spring Security context</li>
 * </ul>
 *
 * <p>The {@code X-Correlation-ID} header is echoed back in the response so that callers can
 * correlate their request with server-side log entries.
 *
 * <p>Ordering: runs at order {@code 1}, i.e. after the Spring Security {@code FilterChainProxy}
 * (order {@code -100}), so the {@code SecurityContext} is already populated when this filter runs.
 */
@Component
@Order(1)
public class MdcContextFilter extends OncePerRequestFilter {

    static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    static final String MDC_CORRELATION_ID = "correlationId";
    static final String MDC_USER_ID = "userId";
    static final String MDC_REQUEST_PATH = "requestPath";
    static final String MDC_DEAL_ID = "dealId";

    private static final Pattern DEAL_ID_PATTERN = Pattern.compile("/deals/([^/?]+)");
    private static final Pattern DEAL_ACTION_PATTERN =
            Pattern.compile("^(batch-sync|submit|approve|reject|cancel|sync)$");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put(MDC_REQUEST_PATH, request.getRequestURI());

            Matcher matcher = DEAL_ID_PATTERN.matcher(request.getRequestURI());
            if (matcher.find()) {
                String segment = matcher.group(1);
                if (!DEAL_ACTION_PATTERN.matcher(segment).matches()) {
                    MDC.put(MDC_DEAL_ID, segment);
                }
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put(MDC_USER_ID, authentication.getName());
            }

            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
