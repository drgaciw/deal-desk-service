package com.aciworldwide.dealdesk.exception;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.exception.RuleValidationException;
import com.aciworldwide.dealdesk.service.impl.DealUpdateException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DealNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDealNotFoundException(DealNotFoundException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Deal not found: {}", correlationId, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Deal Not Found",
                ex.getMessage(), "DEAL_NOT_FOUND", correlationId, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DuplicateDealException.class)
    public ResponseEntity<ProblemDetail> handleDuplicateDealException(DuplicateDealException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Duplicate deal: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.CONFLICT, "Duplicate Deal",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(InvalidDealStateTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDealStateTransitionException(
            InvalidDealStateTransitionException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Invalid deal state transition: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Deal State Transition",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(InvalidDealStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidDealStateException(InvalidDealStateException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Invalid deal state: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Deal State",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(TcvCalculationException.class)
    public ResponseEntity<ProblemDetail> handleTcvCalculationException(TcvCalculationException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] TCV calculation error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "TCV Calculation Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(TCVValidationException.class)
    public ResponseEntity<ProblemDetail> handleTCVValidationException(TCVValidationException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] TCV validation error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "TCV Validation Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(SalesforceIntegrationException.class)
    public ResponseEntity<ProblemDetail> handleSalesforceIntegrationException(
            SalesforceIntegrationException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Salesforce integration error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        if (extra == null) {
            extra = new HashMap<>();
        }
        extra.put("retryable", true);
        extra.put("retryAfterSeconds", 30);
        ProblemDetail problem = buildProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Salesforce Integration Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(SalesforceUpdateException.class)
    public ResponseEntity<ProblemDetail> handleSalesforceUpdateException(SalesforceUpdateException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Salesforce update error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = new HashMap<>();
        extra.put("retryable", true);
        ProblemDetail problem = buildProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Salesforce Update Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(DealUpdateException.class)
    public ResponseEntity<ProblemDetail> handleDealUpdateException(DealUpdateException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Deal update error: {}", correlationId, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Deal Update Error",
                ex.getMessage(), "DEAL_UPDATE_ERROR", correlationId, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(RuleEngineException.class)
    public ResponseEntity<ProblemDetail> handleRuleEngineException(RuleEngineException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Rule engine error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Rule Engine Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(RuleNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleRuleNotFoundException(RuleNotFoundException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Rule not found: {}", correlationId, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.NOT_FOUND, "Rule Not Found",
                ex.getMessage(), "RULE_NOT_FOUND", correlationId, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(RuleValidationException.class)
    public ResponseEntity<ProblemDetail> handleRuleValidationException(RuleValidationException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Rule validation error: {}", correlationId, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Rule Validation Error",
                ex.getMessage(), "RULE_VALIDATION_ERROR", correlationId, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(DealDeskException.class)
    public ResponseEntity<ProblemDetail> handleDealDeskException(DealDeskException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Deal desk error: {}", correlationId, ex.getMessage());
        Map<String, Object> extra = detailsAsExtra(ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Deal Desk Error",
                ex.getMessage(), ex.getErrorCode(), correlationId, extra);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Validation error: {}", correlationId, ex.getMessage());
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (e1, e2) -> e1));
        Map<String, Object> extra = new HashMap<>();
        extra.put("fieldErrors", fieldErrors);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Invalid request parameters", "VALIDATION_ERROR", correlationId, extra);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Constraint violation: {}", correlationId, ex.getMessage());
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (e1, e2) -> e1));
        Map<String, Object> extra = new HashMap<>();
        extra.put("fieldErrors", fieldErrors);
        ProblemDetail problem = buildProblemDetail(HttpStatus.BAD_REQUEST, "Constraint Violation",
                "Constraint violation", "CONSTRAINT_VIOLATION", correlationId, extra);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Access denied: {}", correlationId, ex.getMessage());
        ProblemDetail problem = buildProblemDetail(HttpStatus.FORBIDDEN, "Access Denied",
                "Access denied", "FORBIDDEN", correlationId, null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllUncaughtException(Exception ex, WebRequest request) {
        String correlationId = MDC.get("correlationId");
        log.error("[{}] Unknown error occurred: {}", correlationId, ex.getMessage(), ex);
        ProblemDetail problem = buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", "INTERNAL_ERROR", correlationId, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail,
            String errorCode, String correlationId, Map<String, Object> extraProperties) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());
        if (errorCode != null) {
            problem.setProperty("errorCode", errorCode);
        }
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }
        if (extraProperties != null) {
            extraProperties.forEach(problem::setProperty);
        }
        return problem;
    }

    private Map<String, Object> detailsAsExtra(DealDeskException ex) {
        Object details = ex.getDetails();
        if (details == null) {
            return null;
        }
        Map<String, Object> extra = new HashMap<>();
        extra.put("errorDetails", details);
        return extra;
    }
}
