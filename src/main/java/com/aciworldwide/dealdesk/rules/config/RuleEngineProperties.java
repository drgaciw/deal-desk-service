package com.aciworldwide.dealdesk.rules.config;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "rules")
@Component
public class RuleEngineProperties {

    @NotNull
    private Engine engine = new Engine();

    @NotNull
    private Audit audit = new Audit();

    @NotNull
    private Execution execution = new Execution();

    @NotNull
    private Validation validation = new Validation();

    @NotNull
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Engine {
        private boolean skipOnFirstApplied;
        private boolean skipOnFirstFailed;
        private boolean skipOnFirstNonTriggered;
        
        @Min(1)
        @Max(Integer.MAX_VALUE)
        private int priorityThreshold = Integer.MAX_VALUE;
        
        private boolean silentMode;
        
        @NotNull
        private Versioning versioning = new Versioning();
    }

    @Data
    public static class Versioning {
        private boolean enabled = true;
        
        @Min(1)
        @Max(100)
        private int keepVersions = 5;
    }

    @Data
    public static class Audit {
        private boolean enabled = true;
        
        @Min(1)
        @Max(365)
        private int retentionDays = 30;
        
        @Min(1)
        @Max(1000)
        private int batchSize = 100;
    }

    @Data
    public static class Execution {
        @Min(1)
        @Max(300)
        private int timeoutSeconds = 30;
        
        @Min(value = 1, message = "must be greater than 0")
        @Max(1000)
        private int maxRulesPerCategory = 100;
        
        private boolean parallelExecution;
    }

    @Data
    public static class Validation {
        @Min(100)
        @Max(60000)
        private int expressionTimeoutMs = 5000;
        
        @Min(100)
        @Max(10000)
        private int maxConditionLength = 2000;
        
        @Min(100)
        @Max(10000)
        private int maxActionLength = 2000;
        
        @NotEmpty
        private Set<String> allowedPackages = Set.of(
            "java.math",
            "java.time",
            "java.util",
            "com.aciworldwide.dealdesk.model",
            "com.aciworldwide.dealdesk.rules.model"
        );
    }

    @Data
    public static class Monitoring {
        private boolean metricsEnabled = true;
        private boolean traceEnabled = true;
        
        @Min(100)
        @Max(10000)
        private int alertThresholdMs = 1000;
        
        @Min(1)
        @Max(100)
        private int errorThresholdPercent = 5;
    }
}