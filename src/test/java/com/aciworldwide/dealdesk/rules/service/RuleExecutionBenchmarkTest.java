package com.aciworldwide.dealdesk.rules.service;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.repository.RuleDefinitionRepository;

import org.jeasy.rules.api.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { RuleExecutionBenchmarkTest.TestConfig.class })
public class RuleExecutionBenchmarkTest {

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("rules");
        }

        @Bean
        public RuleExecutionService ruleExecutionService(
                org.jeasy.rules.api.RulesEngine rulesEngine,
                RuleDefinitionRepository ruleRepository,
                List<com.aciworldwide.dealdesk.rules.fact.FactProvider> factProviders) {
            return new RuleExecutionService(rulesEngine, ruleRepository, factProviders);
        }
    }

    @MockitoBean
    private org.jeasy.rules.api.RulesEngine rulesEngine;

    @MockitoBean
    private RuleDefinitionRepository ruleRepository;

    @Autowired
    private RuleExecutionService ruleExecutionService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        if (cacheManager.getCache("rules") != null) {
            cacheManager.getCache("rules").clear();
        }

        List<RuleDefinition> mockRules = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            RuleDefinition def = new RuleDefinition();
            def.setName("Rule" + i);
            def.setConditionExpression("true");
            def.setActionExpression("1 + 1");
            def.setPriority(1);
            mockRules.add(def);
        }
        when(ruleRepository.findAll()).thenReturn(mockRules);
    }

    @Test
    public void benchmarkGetRules() {
        // Warmup (will trigger the first DB call and cache the result)
        for (int i = 0; i < 100; i++) {
            ruleExecutionService.getRules();
        }

        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ruleExecutionService.getRules();
        }
        long end = System.nanoTime();

        System.out.println("Execution time for 1000 getRules() calls WITH CACHING: " + (end - start) / 1_000_000.0 + " ms");

        // Verify repository was called exactly once due to caching
        verify(ruleRepository, times(1)).findAll();
    }
}
