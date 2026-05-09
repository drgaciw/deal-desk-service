package com.aciworldwide.dealdesk.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.exception.RuleValidationException;
import com.aciworldwide.dealdesk.service.impl.DealUpdateException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MDC.put("correlationId", "test-correlation-id");
        webRequest = new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    void testHandleDealNotFoundException() {
        DealNotFoundException ex = new DealNotFoundException("Deal 123 not found");

        ResponseEntity<ProblemDetail> response = handler.handleDealNotFoundException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getDetail()).isEqualTo("Deal not found with id: Deal 123 not found");
        assertThat(body.getProperties()).containsEntry("errorCode", "DEAL_NOT_FOUND");
        assertThat(body.getProperties()).containsEntry("correlationId", "test-correlation-id");
    }

    @Test
    void testHandleDuplicateDealException() {
        DuplicateDealException ex = new DuplicateDealException("OPP-001");

        ResponseEntity<ProblemDetail> response = handler.handleDuplicateDealException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getProperties()).containsEntry("errorCode", "DUPLICATE_DEAL");
    }

    @Test
    void testHandleInvalidDealStateTransitionException() {
        InvalidDealStateTransitionException ex =
                new InvalidDealStateTransitionException(DealStatus.DRAFT, DealStatus.APPROVED);

        ResponseEntity<ProblemDetail> response =
                handler.handleInvalidDealStateTransitionException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(422);
        assertThat(body.getProperties()).containsEntry("errorCode", "INVALID_DEAL_STATE");
    }

    @Test
    void testHandleSalesforceIntegrationException() {
        SalesforceIntegrationException ex = new SalesforceIntegrationException("SF unavailable");

        ResponseEntity<ProblemDetail> response =
                handler.handleSalesforceIntegrationException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(503);
        assertThat(body.getProperties()).containsEntry("retryable", true);
        assertThat(body.getProperties()).containsEntry("retryAfterSeconds", 30);
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("deal", "amount", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ProblemDetail> response = handler.handleValidationException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.getProperties().get("fieldErrors");
        assertThat(fieldErrors).containsEntry("amount", "must not be null");
    }

    @Test
    void testHandleConstraintViolationException() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("dealAmount");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ProblemDetail> response = handler.handleConstraintViolationException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) body.getProperties().get("fieldErrors");
        assertThat(fieldErrors).containsEntry("dealAmount", "must be positive");
    }

    @Test
    void testHandleAccessDeniedException() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Access denied");

        ResponseEntity<ProblemDetail> response = handler.handleAccessDeniedException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(403);
        assertThat(body.getProperties()).containsEntry("errorCode", "FORBIDDEN");
    }

    @Test
    void testHandleUnknownException() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ProblemDetail> response = handler.handleAllUncaughtException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getProperties()).containsEntry("errorCode", "INTERNAL_ERROR");
    }
}
