package com.aciworldwide.dealdesk.rules.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.model.RuleVersion;
import com.aciworldwide.dealdesk.rules.repository.RuleRepository;
import com.aciworldwide.dealdesk.rules.repository.RuleVersionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleVersioningService {
    private final RuleRepository ruleRepository;
    private final RuleVersionRepository versionRepository;

    @Transactional
    public void createVersion(DealRule rule, String modifiedBy, String changeDescription) {
        Assert.notNull(rule, "Rule cannot be null");
        Assert.hasText(modifiedBy, "Modified by cannot be empty");
        Assert.hasText(changeDescription, "Change description cannot be empty");

        try {
            log.debug("Creating version for rule: {} by user: {}", rule.getId(), modifiedBy);
            
            RuleVersion version = RuleVersion.builder()
                .ruleId(rule.getId())
                .version(getNextVersion(rule.getId()))
                .modifiedBy(modifiedBy)
                .modifiedAt(LocalDateTime.now())
                .changeDescription(changeDescription)
                .ruleData(rule)
                .build();

            versionRepository.save(version);
            log.info("Version created for rule: {}, version: {}", rule.getId(), version.getVersion());
        } catch (Exception e) {
            log.error("Failed to create version for rule {}: {}", rule.getId(), e.getMessage());
            throw e;
        }
    }

    public List<RuleVersion> getVersionHistory(String ruleId) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        
        log.debug("Fetching version history for rule: {}", ruleId);
        return versionRepository.findByRuleIdOrderByVersionDesc(ruleId);
    }

    @Transactional
    public DealRule rollbackToVersion(String ruleId, int version) {
        Assert.hasText(ruleId, "Rule ID cannot be empty");
        Assert.isTrue(version > 0, "Version must be greater than 0");

        log.debug("Rolling back rule {} to version {}", ruleId, version);

        RuleVersion ruleVersion = versionRepository.findByRuleIdAndVersion(ruleId, version)
            .orElseThrow(() -> new RuleNotFoundException("Version not found: " + version + " for rule: " + ruleId));

        DealRule rolledBackRule = ruleVersion.getRuleData();
        rolledBackRule.setId(ruleId); // Ensure ID is preserved

        try {
            DealRule savedRule = ruleRepository.save(rolledBackRule);
            log.info("Rule {} rolled back to version {} successfully", ruleId, version);
            return savedRule;
        } catch (Exception e) {
            log.error("Failed to rollback rule {} to version {}: {}", ruleId, version, e.getMessage());
            throw e;
        }
    }

    private int getNextVersion(String ruleId) {
        Optional<RuleVersion> latestVersion = versionRepository.findFirstByRuleIdOrderByVersionDesc(ruleId);
        return latestVersion.map(v -> v.getVersion() + 1).orElse(1);
    }
} 