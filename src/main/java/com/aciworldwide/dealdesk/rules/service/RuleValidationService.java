package com.aciworldwide.dealdesk.rules.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.ConstructorReference;
import org.springframework.expression.spel.ast.TypeReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import com.aciworldwide.dealdesk.rules.config.RuleEngineProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating rule expressions and ensuring they meet security requirements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleValidationService {

    private final RuleEngineProperties properties;
    private final ExecutorService validationExecutor = Executors.newSingleThreadExecutor();
    private final ExpressionParser expressionParser;

    // Patterns for identifying potentially dangerous expressions
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        ".*(?:System|Runtime|Process|ClassLoader|Thread|File|URL|Socket|ServerSocket|" +
        "DriverManager|Statement|Connection|ResultSet).*",
        Pattern.CASE_INSENSITIVE
    );

    public RuleValidationService(RuleEngineProperties properties) {
        this.properties = properties;
        SpelParserConfiguration config = new SpelParserConfiguration(
            SpelCompilerMode.IMMEDIATE,
            this.getClass().getClassLoader(),
            true,
            true,
            Integer.MAX_VALUE
        );
        this.expressionParser = new SpelExpressionParser(config);
    }

    /**
     * Validates a rule expression for syntax, security, and length constraints.
     *
     * @param expression The expression to validate
     * @param isCondition Whether this is a condition expression (vs action)
     * @param context The evaluation context variables
     * @throws IllegalArgumentException if validation fails
     */
    public void validateExpression(String expression, boolean isCondition, Map<String, Object> context) {
        // Check length constraints
        int maxLength = isCondition ? 
            properties.getValidation().getMaxConditionLength() :
            properties.getValidation().getMaxActionLength();
            
        if (expression.length() > maxLength) {
            throw new IllegalArgumentException(String.format(
                "Expression exceeds maximum length of %d characters", maxLength));
        }

        // Check for dangerous patterns
        if (DANGEROUS_PATTERN.matcher(expression).matches()) {
            throw new IllegalArgumentException(
                "Expression contains potentially dangerous operations");
        }

        // Validate syntax and compilation with timeout
        Future<Expression> future = validationExecutor.submit(() -> {
            try {
                Expression parsed = expressionParser.parseExpression(expression);
                StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
                for (Map.Entry<String, Object> entry : context.entrySet()) {
                    evaluationContext.setVariable(entry.getKey(), entry.getValue());
                }
                parsed.getValue(evaluationContext); // Verify it can be evaluated
                return parsed;
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Invalid expression syntax: " + e.getMessage(), e);
            }
        });

        try {
            future.get(properties.getValidation().getExpressionTimeoutMs(), 
                TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IllegalArgumentException(
                "Expression validation timed out - may be too complex");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Expression validation was interrupted");
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause().getMessage(), e.getCause());
        }
    }

    /**
     * Validates a rule expression for syntax, security, and length constraints.
     *
     * @param expression The expression to validate
     * @param isCondition Whether this is a condition expression (vs action)
     * @throws IllegalArgumentException if validation fails
     */
    public void validateExpression(String expression, boolean isCondition) {
        validateExpression(expression, isCondition, new HashMap<>());
    }

    /**
     * Validates that a package is allowed for use in expressions.
     *
     * @param packageName The package name to validate
     * @return true if the package is allowed
     */
    public boolean isPackageAllowed(String packageName) {
        return properties.getValidation().getAllowedPackages().stream()
            .anyMatch(allowed -> packageName.startsWith(allowed));
    }

    /**
     * Validates that all types referenced in an expression are from allowed packages.
     *
     * @param expression The expression to validate
     * @throws IllegalArgumentException if any type is from a disallowed package
     */
    public void validateTypeReferences(String expression) {
        if (expression == null || expression.isBlank()) {
            return;
        }

        try {
            Expression parsed = expressionParser.parseExpression(expression);
            if (parsed instanceof SpelExpression) {
                SpelExpression spelExpression = (SpelExpression) parsed;
                SpelNode ast = spelExpression.getAST();
                validateNode(ast);
            }
        } catch (SpelParseException e) {
             throw new IllegalArgumentException("Invalid expression syntax: " + e.getMessage(), e);
        }
    }

    private void validateNode(SpelNode node) {
        if (node == null) {
            return;
        }

        if (node instanceof TypeReference || node instanceof ConstructorReference) {
            if (node.getChildCount() > 0) {
                SpelNode child = node.getChild(0);
                String typeName = child.toStringAST();

                int lastDot = typeName.lastIndexOf('.');
                if (lastDot > 0) {
                    String pkg = typeName.substring(0, lastDot);
                    if (!isPackageAllowed(pkg)) {
                        throw new IllegalArgumentException(
                            "Referenced type from disallowed package: " + pkg);
                    }
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            validateNode(node.getChild(i));
        }
    }

    /**
     * Performs comprehensive validation of a rule expression.
     *
     * @param expression The expression to validate
     * @param isCondition Whether this is a condition expression
     * @throws IllegalArgumentException if validation fails
     */
    public void validateRuleExpression(String expression, boolean isCondition) {
        validateExpression(expression, isCondition);
        validateTypeReferences(expression);
    }
}
