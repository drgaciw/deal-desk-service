package com.aciworldwide.dealdesk.rules.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.aciworldwide.dealdesk.exception.GlobalExceptionHandler;
import com.aciworldwide.dealdesk.rules.model.RuleDefinition;
import com.aciworldwide.dealdesk.rules.service.RuleDefinitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleDefinitionController MockMvc Tests")
class RuleDefinitionControllerTest {

    @Mock
    private RuleDefinitionService ruleDefinitionService;

    @InjectMocks
    private RuleDefinitionController ruleDefinitionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private RuleDefinition validRule;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(ruleDefinitionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        validRule = RuleDefinition.builder()
                .id("rule-id-001")
                .ruleKey("TEST_RULE")
                .name("Test Rule")
                .description("A test rule description")
                .category("PRICING")
                .priority(100)
                .conditionExpression("true")
                .actionExpression("#deal.getValue()")
                .enabled(true)
                .parameters(new HashMap<>())
                .validFrom(LocalDateTime.now())
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .lastModifiedBy("admin")
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

    private String ruleJson() throws Exception {
        return objectMapper.writeValueAsString(validRule);
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/rules
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/rules")
    class CreateRule {

        @Test
        @DisplayName("creates rule and returns 200 with rule body")
        void createRule_ValidRule_Returns200() throws Exception {
            when(ruleDefinitionService.createRule(any(RuleDefinition.class))).thenReturn(validRule);

            mockMvc.perform(post("/api/v1/rules")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ruleJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ruleKey").value("TEST_RULE"))
                    .andExpect(jsonPath("$.name").value("Test Rule"));
        }

        @Test
        @DisplayName("returns 500 when rule key already exists")
        void createRule_DuplicateKey_Returns500() throws Exception {
            when(ruleDefinitionService.createRule(any(RuleDefinition.class)))
                    .thenThrow(new IllegalArgumentException("Rule with key TEST_RULE already exists"));

            mockMvc.perform(post("/api/v1/rules")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ruleJson()))
                    .andExpect(status().isInternalServerError());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/rules/{ruleKey}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/rules/{ruleKey}")
    class UpdateRule {

        @Test
        @DisplayName("updates rule and returns 200 with updated body")
        void updateRule_ValidRule_Returns200() throws Exception {
            when(ruleDefinitionService.updateRule(eq("TEST_RULE"), any(RuleDefinition.class)))
                    .thenReturn(validRule);

            mockMvc.perform(put("/api/v1/rules/TEST_RULE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ruleJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ruleKey").value("TEST_RULE"));
        }

        @Test
        @DisplayName("returns 500 when rule not found")
        void updateRule_NotFound_Returns500() throws Exception {
            when(ruleDefinitionService.updateRule(eq("MISSING_KEY"), any(RuleDefinition.class)))
                    .thenThrow(new IllegalArgumentException("Rule not found with key: MISSING_KEY"));

            mockMvc.perform(put("/api/v1/rules/MISSING_KEY")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(ruleJson()))
                    .andExpect(status().isInternalServerError());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/rules/{ruleKey}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/rules/{ruleKey}")
    class DeleteRule {

        @Test
        @DisplayName("deletes rule and returns 200")
        void deleteRule_ExistingRule_Returns200() throws Exception {
            doNothing().when(ruleDefinitionService).deleteRule("TEST_RULE");

            mockMvc.perform(delete("/api/v1/rules/TEST_RULE"))
                    .andExpect(status().isOk());

            verify(ruleDefinitionService).deleteRule("TEST_RULE");
        }

        @Test
        @DisplayName("returns 500 when rule not found")
        void deleteRule_NotFound_Returns500() throws Exception {
            doThrow(new IllegalArgumentException("Rule not found with key: MISSING_KEY"))
                    .when(ruleDefinitionService).deleteRule("MISSING_KEY");

            mockMvc.perform(delete("/api/v1/rules/MISSING_KEY"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rules/{ruleKey}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/rules/{ruleKey}")
    class GetRule {

        @Test
        @DisplayName("returns 200 with rule when found")
        void getRule_ExistingKey_Returns200() throws Exception {
            when(ruleDefinitionService.getRule("TEST_RULE")).thenReturn(Optional.of(validRule));

            mockMvc.perform(get("/api/v1/rules/TEST_RULE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ruleKey").value("TEST_RULE"))
                    .andExpect(jsonPath("$.category").value("PRICING"));
        }

        @Test
        @DisplayName("returns 404 when rule not found")
        void getRule_NotFound_Returns404() throws Exception {
            when(ruleDefinitionService.getRule("MISSING_KEY")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/rules/MISSING_KEY"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rules/category/{category}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/rules/category/{category}")
    class GetRulesByCategory {

        @Test
        @DisplayName("returns list of rules for category")
        void getRulesByCategory_ExistingCategory_Returns200() throws Exception {
            List<RuleDefinition> rules = List.of(validRule);
            when(ruleDefinitionService.getRulesByCategory("PRICING")).thenReturn(rules);

            mockMvc.perform(get("/api/v1/rules/category/PRICING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].ruleKey").value("TEST_RULE"));
        }

        @Test
        @DisplayName("returns empty list when no rules in category")
        void getRulesByCategory_EmptyCategory_ReturnsEmptyList() throws Exception {
            when(ruleDefinitionService.getRulesByCategory("EMPTY")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/rules/category/EMPTY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rules/category/{category}/active
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/rules/category/{category}/active")
    class GetActiveRules {

        @Test
        @DisplayName("returns active rules for category")
        void getActiveRules_ExistingCategory_ReturnsActiveRules() throws Exception {
            List<RuleDefinition> rules = List.of(validRule);
            when(ruleDefinitionService.getActiveRules("PRICING")).thenReturn(rules);

            mockMvc.perform(get("/api/v1/rules/category/PRICING/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("returns empty list when no active rules in category")
        void getActiveRules_NoActiveRules_ReturnsEmpty() throws Exception {
            when(ruleDefinitionService.getActiveRules("INACTIVE_CAT")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/rules/category/INACTIVE_CAT/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rules/categories
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/rules/categories")
    class GetCategories {

        @Test
        @DisplayName("returns list of all active categories")
        void getActiveCategories_Returns200WithCategories() throws Exception {
            List<String> categories = List.of("PRICING", "VALIDATION", "STATUS");
            when(ruleDefinitionService.getActiveCategories()).thenReturn(categories);

            mockMvc.perform(get("/api/v1/rules/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0]").value("PRICING"));
        }

        @Test
        @DisplayName("returns empty list when no active categories")
        void getActiveCategories_NoCategories_ReturnsEmpty() throws Exception {
            when(ruleDefinitionService.getActiveCategories()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/rules/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
