package com.aciworldwide.dealdesk.rules.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aciworldwide.dealdesk.rules.exception.RuleNotFoundException;
import com.aciworldwide.dealdesk.rules.model.DealRule;
import com.aciworldwide.dealdesk.rules.model.RuleVersion;
import com.aciworldwide.dealdesk.rules.repository.RuleRepository;
import com.aciworldwide.dealdesk.rules.repository.RuleVersionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleVersioningService Tests")
class RuleVersioningServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @Mock
    private RuleVersionRepository versionRepository;

    @InjectMocks
    private RuleVersioningService ruleVersioningService;

    private DealRule sampleRule;

    @BeforeEach
    void setUp() {
        sampleRule = DealRule.builder()
                .id("rule-001")
                .name("Test Rule")
                .description("A test rule")
                .active(true)
                .version(1)
                .category("PRICING")
                .priority(10)
                .build();
    }

    // -------------------------------------------------------------------------
    // createVersion
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createVersion")
    class CreateVersion {

        @Test
        @DisplayName("creates version with correct metadata and saves it")
        void createVersion_ValidInputs_SavesVersion() {
            when(versionRepository.findFirstByRuleIdOrderByVersionDesc("rule-001"))
                    .thenReturn(Optional.empty());

            ruleVersioningService.createVersion(sampleRule, "user1", "Initial version");

            ArgumentCaptor<RuleVersion> captor = ArgumentCaptor.forClass(RuleVersion.class);
            verify(versionRepository).save(captor.capture());

            RuleVersion saved = captor.getValue();
            assertThat(saved.getRuleId()).isEqualTo("rule-001");
            assertThat(saved.getVersion()).isEqualTo(1);
            assertThat(saved.getModifiedBy()).isEqualTo("user1");
            assertThat(saved.getChangeDescription()).isEqualTo("Initial version");
            assertThat(saved.getModifiedAt()).isNotNull();
            assertThat(saved.getRuleData()).isEqualTo(sampleRule);
        }

        @Test
        @DisplayName("increments version number when previous versions exist")
        void createVersion_PreviousVersionExists_IncrementsVersion() {
            RuleVersion existing = RuleVersion.builder()
                    .ruleId("rule-001")
                    .version(3)
                    .build();
            when(versionRepository.findFirstByRuleIdOrderByVersionDesc("rule-001"))
                    .thenReturn(Optional.of(existing));

            ruleVersioningService.createVersion(sampleRule, "user1", "Update");

            ArgumentCaptor<RuleVersion> captor = ArgumentCaptor.forClass(RuleVersion.class);
            verify(versionRepository).save(captor.capture());
            assertThat(captor.getValue().getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("throws NullPointerException when rule is null")
        void createVersion_NullRule_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.createVersion(null, "user1", "description"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when modifiedBy is blank")
        void createVersion_BlankModifiedBy_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.createVersion(sampleRule, "", "description"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when changeDescription is blank")
        void createVersion_BlankChangeDescription_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.createVersion(sampleRule, "user1", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // -------------------------------------------------------------------------
    // getVersionHistory
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getVersionHistory")
    class GetVersionHistory {

        @Test
        @DisplayName("returns ordered version list from repository")
        void getVersionHistory_ExistingRule_ReturnsVersions() {
            List<RuleVersion> versions = List.of(
                    RuleVersion.builder().ruleId("rule-001").version(2).build(),
                    RuleVersion.builder().ruleId("rule-001").version(1).build());
            when(versionRepository.findByRuleIdOrderByVersionDesc("rule-001")).thenReturn(versions);

            List<RuleVersion> result = ruleVersioningService.getVersionHistory("rule-001");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getVersion()).isEqualTo(2);
            assertThat(result.get(1).getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty list when no versions found")
        void getVersionHistory_NoVersions_ReturnsEmpty() {
            when(versionRepository.findByRuleIdOrderByVersionDesc("rule-001"))
                    .thenReturn(List.of());

            List<RuleVersion> result = ruleVersioningService.getVersionHistory("rule-001");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("throws when ruleId is blank")
        void getVersionHistory_BlankRuleId_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.getVersionHistory(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // -------------------------------------------------------------------------
    // rollbackToVersion
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("rollbackToVersion")
    class RollbackToVersion {

        @Test
        @DisplayName("rolls back to specific version and saves")
        void rollbackToVersion_ValidVersion_SavesRolledBackRule() {
            DealRule ruleData = DealRule.builder()
                    .id("rule-001")
                    .name("Old Name")
                    .version(1)
                    .build();
            RuleVersion version = RuleVersion.builder()
                    .ruleId("rule-001")
                    .version(2)
                    .ruleData(ruleData)
                    .build();
            when(versionRepository.findByRuleIdAndVersion("rule-001", 2))
                    .thenReturn(Optional.of(version));
            when(ruleRepository.save(any(DealRule.class))).thenReturn(ruleData);

            DealRule result = ruleVersioningService.rollbackToVersion("rule-001", 2);

            assertThat(result.getId()).isEqualTo("rule-001");
            verify(ruleRepository).save(ruleData);
        }

        @Test
        @DisplayName("preserves original rule ID in rolled-back rule")
        void rollbackToVersion_PreservesRuleId() {
            DealRule ruleData = DealRule.builder().name("Old Name").build();
            RuleVersion version = RuleVersion.builder()
                    .ruleId("rule-001")
                    .version(1)
                    .ruleData(ruleData)
                    .build();
            when(versionRepository.findByRuleIdAndVersion("rule-001", 1))
                    .thenReturn(Optional.of(version));
            when(ruleRepository.save(any(DealRule.class))).thenAnswer(inv -> inv.getArgument(0));

            DealRule result = ruleVersioningService.rollbackToVersion("rule-001", 1);

            assertThat(result.getId()).isEqualTo("rule-001");
        }

        @Test
        @DisplayName("throws RuleNotFoundException when version not found")
        void rollbackToVersion_VersionNotFound_Throws() {
            when(versionRepository.findByRuleIdAndVersion("rule-001", 99))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> ruleVersioningService.rollbackToVersion("rule-001", 99))
                    .isInstanceOf(RuleNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("throws when ruleId is blank")
        void rollbackToVersion_BlankRuleId_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.rollbackToVersion("", 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws when version is zero or negative")
        void rollbackToVersion_InvalidVersion_Throws() {
            assertThatThrownBy(() -> ruleVersioningService.rollbackToVersion("rule-001", 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
