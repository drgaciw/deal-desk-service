package com.aciworldwide.dealdesk.rules.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.rules.cache.RuleCache;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.repository.RuleRepository;

@ExtendWith(MockitoExtension.class)
class RuleCachingServiceTest {

    @Mock
    private RuleCache ruleCache;

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleCachingService ruleCachingService;

    @Test
    @DisplayName("Should return cached rule when available")
    void shouldReturnCachedRule() {
        // Arrange
        String ruleId = "test-id";
        DealRule cachedRule = DealRule.builder()
            .id(ruleId)
            .name("Cached Rule")
            .build();
        
        when(ruleCache.get(ruleId)).thenReturn(Optional.of(cachedRule));

        // Act
        DealRule result = ruleCachingService.getRule(ruleId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ruleId);
        verify(ruleRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fetch from repository when cache miss")
    void shouldFetchFromRepositoryOnCacheMiss() {
        // Arrange
        String ruleId = "test-id";
        DealRule repositoryRule = DealRule.builder()
            .id(ruleId)
            .name("Repository Rule")
            .build();
        
        when(ruleCache.get(ruleId)).thenReturn(Optional.empty());
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.of(repositoryRule));

        // Act
        DealRule result = ruleCachingService.getRule(ruleId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ruleId);
        verify(ruleRepository).findById(ruleId);
        verify(ruleCache).put(ruleId, repositoryRule);
    }

    @Test
    @DisplayName("Should save and cache new rule")
    void shouldSaveAndCacheNewRule() {
        // Arrange
        String ruleId = "test-id";
        DealRule newRule = DealRule.builder()
            .id(ruleId)
            .name("New Rule")
            .build();
        
        when(ruleRepository.save(any(DealRule.class))).thenReturn(newRule);

        // Act
        DealRule result = ruleCachingService.saveRule(newRule);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ruleId);
        verify(ruleRepository).save(newRule);
        verify(ruleCache).put(ruleId, newRule);
    }

    @Test
    @DisplayName("Should invalidate cached rule")
    void shouldInvalidateCachedRule() {
        // Arrange
        String ruleId = "test-id";

        // Act
        ruleCachingService.invalidateRule(ruleId);

        // Assert
        verify(ruleCache).evict(ruleId);
    }

    @Test
    @DisplayName("Should invalidate all cached rules")
    void shouldInvalidateAllRules() {
        // Act
        ruleCachingService.invalidateAllRules();

        // Assert
        verify(ruleCache).clear();
    }
} 