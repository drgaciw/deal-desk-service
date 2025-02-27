package com.aciworldwide.dealdesk.controller;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import com.aciworldwide.dealdesk.util.TestJwtTokens;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.jeasy.rules.api.Rules;
import com.aciworldwide.dealdesk.mapper.DealMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@ExtendWith(MockitoExtension.class)
class DealControllerIntegrationTest {

    private static final String BASE_URL = "/v1/deals";
    private static final String TEST_USER_ID = "test-user";
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DealRepository dealRepository;

    @Mock
    private Rules rules;

    @Mock
    private DealMapper dealMapper;

    @BeforeAll
    void setUp() {
        given(rules.isEmpty()).willReturn(false);
    }

    @BeforeEach
    void setup() {
        dealRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        dealRepository.deleteAll();
    }

    @Nested
    @DisplayName("Deal Creation Tests")
    class DealCreationTests {
        
        @Test
        @DisplayName("Should create deal successfully with valid token and request")
        void shouldCreateDealSuccessfully() throws Exception {
            // Given
            DealRequestDTO request = TestDataFactory.createDealRequest();
            String token = TestJwtTokens.createDealCreatorToken();

            // When
            MvcResult result = mockMvc.perform(post(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-User-ID", TEST_USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(DealStatus.DRAFT.name()))
                .andExpect(jsonPath("$.createdBy").value(TEST_USER_ID))
                .andReturn();

            // Then
            DealResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                DealResponseDTO.class
            );

            assertThat(response)
                .satisfies(r -> {
                    assertThat(r.getName()).isEqualTo(request.getName());
                    assertThat(r.getSalesforceOpportunityId()).isEqualTo(request.getSalesforceOpportunityId());
                    assertThat(r.getValue()).isEqualByComparingTo(request.getValue());
                    assertThat(r.getCreatedAt()).isNotNull();
                });
        }

        @Test
        @DisplayName("Should return 401 with invalid token")
        void shouldReturn401WithInvalidToken() throws Exception {
            DealRequestDTO request = TestDataFactory.createDealRequest();
            String token = TestJwtTokens.createInvalidToken();

            mockMvc.perform(post(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 with invalid request")
        void shouldReturn400WithInvalidRequest() throws Exception {
            String token = TestJwtTokens.createDealCreatorToken();
            DealRequestDTO invalidRequest = new DealRequestDTO();

            mockMvc.perform(post(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
        }
    }

    @Nested
    @DisplayName("Deal Status Update Tests")
    class DealStatusUpdateTests {

        @Test
        @DisplayName("Should approve deal successfully with valid approver token")
        void shouldApproveDealSuccessfully() throws Exception {
            // Given
            Deal deal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            deal = dealRepository.save(deal);
            String token = TestJwtTokens.createDealApproverToken();

            // When/Then
            mockMvc.perform(post(BASE_URL + "/{id}/approve", deal.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-User-ID", "test-approver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DealStatus.APPROVED.name()))
                .andExpect(jsonPath("$.approvedBy").value("test-approver"))
                .andExpect(jsonPath("$.approvedAt").exists());
        }

        @Test
        @DisplayName("Should reject deal successfully with valid approver token")
        void shouldRejectDealSuccessfully() throws Exception {
            // Given
            Deal deal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            deal = dealRepository.save(deal);
            String token = TestJwtTokens.createDealApproverToken();
            String rejectionReason = "Budget constraints";

            // When/Then
            mockMvc.perform(post(BASE_URL + "/{id}/reject", deal.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-User-ID", "test-approver")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"reason\": \"%s\"}", rejectionReason)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(DealStatus.REJECTED.name()))
                .andExpect(jsonPath("$.rejectedBy").value("test-approver"))
                .andExpect(jsonPath("$.rejectionReason").value(rejectionReason));
        }
    }

    @Nested
    @DisplayName("Deal Retrieval Tests")
    class DealRetrievalTests {

        @ParameterizedTest
        @EnumSource(DealStatus.class)
        @DisplayName("Should return deals filtered by status")
        void shouldReturnDealsFilteredByStatus(DealStatus status) throws Exception {
            // Given
            List<Deal> deals = TestDataFactory.createDealsWithStatus(status, 3);
            dealRepository.saveAll(deals);
            String token = TestJwtTokens.createDealViewerToken();

            // When/Then
            mockMvc.perform(get(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].status").value(status.name()));
        }

        @Test
        @DisplayName("Should return paginated results")
        void shouldReturnPaginatedResults() throws Exception {
            // Given
            List<Deal> deals = TestDataFactory.createDeals(15);
            dealRepository.saveAll(deals);
            String token = TestJwtTokens.createDealViewerToken();

            // When/Then
            mockMvc.perform(get(BASE_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .param("page", "0")
                    .param("size", String.valueOf(DEFAULT_PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(DEFAULT_PAGE_SIZE)));
        }
    }

    @Nested
    @DisplayName("Deal Update Tests")
    class DealUpdateTests {

        @Test
        @DisplayName("Should update deal successfully with valid token")
        void shouldUpdateDealSuccessfully() throws Exception {
            // Given
            Deal existingDeal = dealRepository.save(TestDataFactory.createDeal());
            DealRequestDTO updateRequest = TestDataFactory.createDealRequest();
            updateRequest.setName("Updated Name");
            updateRequest.setValue(BigDecimal.valueOf(1000000));
            String token = TestJwtTokens.createDealCreatorToken();

            // When/Then
            mockMvc.perform(put(BASE_URL + "/{id}", existingDeal.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-User-ID", TEST_USER_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.value").value(updateRequest.getValue().intValue()))
                .andExpect(jsonPath("$.lastModifiedBy").value(TEST_USER_ID))
                .andExpect(jsonPath("$.lastModifiedAt").exists());
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent deal")
        void shouldReturn404WhenUpdatingNonExistentDeal() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            DealRequestDTO updateRequest = TestDataFactory.createDealRequest();
            String token = TestJwtTokens.createDealCreatorToken();

            // When/Then
            mockMvc.perform(put(BASE_URL + "/{id}", nonExistentId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Deal Deletion Tests")
    class DealDeletionTests {

        @Test
        @DisplayName("Should delete deal successfully with admin token")
        void shouldDeleteDealSuccessfully() throws Exception {
            // Given
            Deal deal = dealRepository.save(TestDataFactory.createDeal());
            String token = TestJwtTokens.createDealAdminToken();

            // When/Then
            mockMvc.perform(delete(BASE_URL + "/{id}", deal.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

            assertThat(dealRepository.findById(deal.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should return 403 when non-admin attempts deletion")
        void shouldReturn403WhenNonAdminAttemptsDeletion() throws Exception {
            // Given
            Deal deal = dealRepository.save(TestDataFactory.createDeal());
            String token = TestJwtTokens.createDealCreatorToken();

            // When/Then
            mockMvc.perform(delete(BASE_URL + "/{id}", deal.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        }
    }
}