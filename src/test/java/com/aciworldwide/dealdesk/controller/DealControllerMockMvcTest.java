package com.aciworldwide.dealdesk.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

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

import com.aciworldwide.dealdesk.exception.DealNotFoundException;
import com.aciworldwide.dealdesk.exception.GlobalExceptionHandler;
import com.aciworldwide.dealdesk.exception.InvalidDealStateException;
import com.aciworldwide.dealdesk.mapper.DealMapper;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.service.DealService;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealController MockMvc Tests")
class DealControllerMockMvcTest {

    @Mock
    private DealService dealService;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Deal testDeal;
    private DealResponseDTO testResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        testDeal = TestDataFactory.createDeal();
        testDeal.setId("deal-001");

        testResponse = new DealResponseDTO();
        testResponse.setId("deal-001");
        testResponse.setName(testDeal.getName());
        testResponse.setStatus(DealStatus.DRAFT);
        testResponse.setValue(testDeal.getValue());
        testResponse.setSalesforceOpportunityId(testDeal.getSalesforceOpportunityId());
        testResponse.setAccountId(testDeal.getAccountId());
        testResponse.setAccountName(testDeal.getAccountName());
        testResponse.setSalesRepId(testDeal.getSalesRepId());
        testResponse.setSalesRepName(testDeal.getSalesRepName());
        testResponse.setCreatedAt(ZonedDateTime.now());
    }

    private String validDealRequestJson() throws Exception {
        return objectMapper.writeValueAsString(TestDataFactory.createDealRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals - Create deal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals")
    class CreateDeal {

        @Test
        @DisplayName("creates deal and returns 201 with response body")
        void createDeal_ValidRequest_Returns201() throws Exception {
            when(dealMapper.toDeal(any())).thenReturn(testDeal);
            when(dealService.createDeal(any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toResponseDTO(any(Deal.class))).thenReturn(testResponse);

            mockMvc.perform(post("/api/v1/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validDealRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value("deal-001"))
                    .andExpect(jsonPath("$.status").value("DRAFT"));
        }

        @Test
        @DisplayName("returns 400 when request body is missing required fields")
        void createDeal_MissingRequiredFields_Returns400() throws Exception {
            mockMvc.perform(post("/api/v1/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 400 when deal already exists for opportunity")
        void createDeal_DuplicateOpportunity_Returns400() throws Exception {
            when(dealMapper.toDeal(any())).thenReturn(testDeal);
            when(dealService.createDeal(any(Deal.class)))
                    .thenThrow(new IllegalArgumentException("Deal already exists for opportunity: OPP-123"));

            mockMvc.perform(post("/api/v1/deals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validDealRequestJson()))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/deals/{id} - Get by ID
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/deals/{id}")
    class GetDeal {

        @Test
        @DisplayName("returns 200 with deal when found")
        void getDeal_ExistingId_Returns200() throws Exception {
            when(dealService.getDealById("deal-001")).thenReturn(testDeal);
            when(dealMapper.toResponseDTO(testDeal)).thenReturn(testResponse);

            mockMvc.perform(get("/api/v1/deals/deal-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("deal-001"));
        }

        @Test
        @DisplayName("returns 404 when deal not found")
        void getDeal_NotFound_Returns404() throws Exception {
            when(dealService.getDealById("missing"))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(get("/api/v1/deals/missing"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/deals/{id} - Update deal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/deals/{id}")
    class UpdateDeal {

        @Test
        @DisplayName("updates deal and returns 200 with updated body")
        void updateDeal_ValidRequest_Returns200() throws Exception {
            when(dealMapper.toDeal(any())).thenReturn(testDeal);
            when(dealService.updateDeal(eq("deal-001"), any(Deal.class))).thenReturn(testDeal);
            when(dealMapper.toResponseDTO(any(Deal.class))).thenReturn(testResponse);

            mockMvc.perform(put("/api/v1/deals/deal-001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validDealRequestJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("deal-001"));
        }

        @Test
        @DisplayName("returns 404 when deal to update is not found")
        void updateDeal_NotFound_Returns404() throws Exception {
            when(dealMapper.toDeal(any())).thenReturn(testDeal);
            when(dealService.updateDeal(eq("missing"), any(Deal.class)))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(put("/api/v1/deals/missing")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validDealRequestJson()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when updated deal has invalid state")
        void updateDeal_InvalidState_Returns400() throws Exception {
            when(dealMapper.toDeal(any())).thenReturn(testDeal);
            when(dealService.updateDeal(eq("deal-001"), any(Deal.class)))
                    .thenThrow(new InvalidDealStateException("Cannot update deal in current state"));

            mockMvc.perform(put("/api/v1/deals/deal-001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validDealRequestJson()))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/deals/{id} - Delete deal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/deals/{id}")
    class DeleteDeal {

        @Test
        @DisplayName("deletes deal and returns 204")
        void deleteDeal_ExistingDraft_Returns204() throws Exception {
            doNothing().when(dealService).deleteDeal("deal-001");

            mockMvc.perform(delete("/api/v1/deals/deal-001"))
                    .andExpect(status().isNoContent());

            verify(dealService).deleteDeal("deal-001");
        }

        @Test
        @DisplayName("returns 404 when deal to delete is not found")
        void deleteDeal_NotFound_Returns404() throws Exception {
            doThrow(new DealNotFoundException("Deal not found with id: missing"))
                    .when(dealService).deleteDeal("missing");

            mockMvc.perform(delete("/api/v1/deals/missing"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns 400 when deal is not in DRAFT state")
        void deleteDeal_NotDraft_Returns400() throws Exception {
            doThrow(new InvalidDealStateException(DealStatus.SUBMITTED, "delete"))
                    .when(dealService).deleteDeal("deal-001");

            mockMvc.perform(delete("/api/v1/deals/deal-001"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/deals - Search / list deals
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/deals")
    class SearchDeals {

        @Test
        @DisplayName("returns all deals when no filters provided")
        void searchDeals_NoFilters_ReturnsAll() throws Exception {
            List<Deal> deals = TestDataFactory.createDeals(3);
            List<DealResponseDTO> responseDTOs = deals.stream().map(d -> {
                DealResponseDTO dto = new DealResponseDTO();
                dto.setId(d.getId());
                return dto;
            }).toList();
            when(dealService.getAllDeals()).thenReturn(deals);
            when(dealMapper.toResponseDTOList(deals)).thenReturn(responseDTOs);

            mockMvc.perform(get("/api/v1/deals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        @DisplayName("filters by status when status param is provided")
        void searchDeals_WithStatusFilter_FiltersByStatus() throws Exception {
            List<Deal> deals = TestDataFactory.createDealsWithStatus(DealStatus.APPROVED, 2);
            List<DealResponseDTO> responseDTOs = List.of(new DealResponseDTO(), new DealResponseDTO());
            when(dealService.getDealsByStatus(DealStatus.APPROVED)).thenReturn(deals);
            when(dealMapper.toResponseDTOList(deals)).thenReturn(responseDTOs);

            mockMvc.perform(get("/api/v1/deals").param("status", "APPROVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("filters by accountId when accountId param is provided")
        void searchDeals_WithAccountIdFilter_FiltersByAccount() throws Exception {
            List<Deal> deals = List.of(TestDataFactory.createDealForAccount("ACC-123"));
            when(dealService.getDealsByAccount("ACC-123")).thenReturn(deals);
            when(dealMapper.toResponseDTOList(deals)).thenReturn(List.of(new DealResponseDTO()));

            mockMvc.perform(get("/api/v1/deals").param("accountId", "ACC-123"))
                    .andExpect(status().isOk());

            verify(dealService).getDealsByAccount("ACC-123");
        }

        @Test
        @DisplayName("filters by salesRepId when salesRepId param is provided")
        void searchDeals_WithSalesRepFilter_FiltersBySalesRep() throws Exception {
            List<Deal> deals = List.of(TestDataFactory.createDealForSalesRep("REP-456"));
            when(dealService.getDealsBySalesRep("REP-456")).thenReturn(deals);
            when(dealMapper.toResponseDTOList(deals)).thenReturn(List.of(new DealResponseDTO()));

            mockMvc.perform(get("/api/v1/deals").param("salesRepId", "REP-456"))
                    .andExpect(status().isOk());

            verify(dealService).getDealsBySalesRep("REP-456");
        }

        @Test
        @DisplayName("filters by minValue when minValue param is provided")
        void searchDeals_WithMinValueFilter_FiltersHighValueDeals() throws Exception {
            List<Deal> deals = List.of(TestDataFactory.createHighValueDeal());
            when(dealService.getHighValueDeals(any(BigDecimal.class), eq(DealStatus.APPROVED)))
                    .thenReturn(deals);
            when(dealMapper.toResponseDTOList(deals)).thenReturn(List.of(new DealResponseDTO()));

            mockMvc.perform(get("/api/v1/deals").param("minValue", "1000000"))
                    .andExpect(status().isOk());

            verify(dealService).getHighValueDeals(any(BigDecimal.class), eq(DealStatus.APPROVED));
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals/{id}/submit
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals/{id}/submit")
    class SubmitDeal {

        @Test
        @DisplayName("submits deal for approval and returns 200")
        void submitForApproval_ValidDeal_Returns200() throws Exception {
            Deal submittedDeal = TestDataFactory.createDealWithStatus(DealStatus.SUBMITTED);
            submittedDeal.setId("deal-001");
            DealResponseDTO submittedResponse = new DealResponseDTO();
            submittedResponse.setId("deal-001");
            submittedResponse.setStatus(DealStatus.SUBMITTED);
            when(dealService.submitForApproval("deal-001")).thenReturn(submittedDeal);
            when(dealMapper.toResponseDTO(submittedDeal)).thenReturn(submittedResponse);

            mockMvc.perform(post("/api/v1/deals/deal-001/submit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUBMITTED"));
        }

        @Test
        @DisplayName("returns 400 when deal is not in DRAFT state")
        void submitForApproval_NotDraft_Returns400() throws Exception {
            when(dealService.submitForApproval("deal-001"))
                    .thenThrow(new InvalidDealStateException("Cannot submit for approval deal in status: SUBMITTED"));

            mockMvc.perform(post("/api/v1/deals/deal-001/submit"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when deal not found")
        void submitForApproval_NotFound_Returns404() throws Exception {
            when(dealService.submitForApproval("missing"))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(post("/api/v1/deals/missing/submit"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals/{id}/approve
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals/{id}/approve")
    class ApproveDeal {

        @Test
        @DisplayName("approves deal and returns 200 with approved status")
        void approveDeal_ValidSubmittedDeal_Returns200() throws Exception {
            Deal approvedDeal = TestDataFactory.createDealWithStatus(DealStatus.APPROVED);
            approvedDeal.setId("deal-001");
            approvedDeal.setApprovedBy("approver-user");
            DealResponseDTO approvedResponse = new DealResponseDTO();
            approvedResponse.setId("deal-001");
            approvedResponse.setStatus(DealStatus.APPROVED);
            approvedResponse.setApprovedBy("approver-user");
            when(dealService.approveDeal("deal-001", "approver-user")).thenReturn(approvedDeal);
            when(dealMapper.toResponseDTO(approvedDeal)).thenReturn(approvedResponse);

            mockMvc.perform(post("/api/v1/deals/deal-001/approve")
                    .header("X-User-ID", "approver-user"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"))
                    .andExpect(jsonPath("$.approvedBy").value("approver-user"));
        }

        @Test
        @DisplayName("returns 400 when deal is not in SUBMITTED state")
        void approveDeal_NotSubmitted_Returns400() throws Exception {
            when(dealService.approveDeal(eq("deal-001"), anyString()))
                    .thenThrow(new InvalidDealStateException("Cannot approve deal in status: DRAFT"));

            mockMvc.perform(post("/api/v1/deals/deal-001/approve")
                    .header("X-User-ID", "approver-user"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when deal not found")
        void approveDeal_NotFound_Returns404() throws Exception {
            when(dealService.approveDeal(eq("missing"), anyString()))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(post("/api/v1/deals/missing/approve")
                    .header("X-User-ID", "approver-user"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals/{id}/reject
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals/{id}/reject")
    class RejectDeal {

        @Test
        @DisplayName("rejects deal and returns 200 with rejected status")
        void rejectDeal_ValidDeal_Returns200() throws Exception {
            Deal rejectedDeal = TestDataFactory.createDealWithStatus(DealStatus.REJECTED);
            rejectedDeal.setId("deal-001");
            DealResponseDTO rejectedResponse = new DealResponseDTO();
            rejectedResponse.setId("deal-001");
            rejectedResponse.setStatus(DealStatus.REJECTED);
            when(dealService.rejectDeal(eq("deal-001"), anyString(), anyString()))
                    .thenReturn(rejectedDeal);
            when(dealMapper.toResponseDTO(rejectedDeal)).thenReturn(rejectedResponse);

            mockMvc.perform(post("/api/v1/deals/deal-001/reject")
                    .header("X-User-ID", "approver-user")
                    .param("reason", "Price too high"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        @DisplayName("returns 404 when deal not found")
        void rejectDeal_NotFound_Returns404() throws Exception {
            when(dealService.rejectDeal(eq("missing"), anyString(), anyString()))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(post("/api/v1/deals/missing/reject")
                    .header("X-User-ID", "approver-user")
                    .param("reason", "Price too high"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals/{id}/sync - Salesforce sync
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals/{id}/sync")
    class SyncDeal {

        @Test
        @DisplayName("syncs deal with Salesforce and returns 200")
        void syncDeal_ValidDeal_Returns200() throws Exception {
            when(dealService.syncWithSalesforce("deal-001")).thenReturn(testDeal);
            when(dealMapper.toResponseDTO(testDeal)).thenReturn(testResponse);

            mockMvc.perform(post("/api/v1/deals/deal-001/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("deal-001"));
        }

        @Test
        @DisplayName("returns 404 when deal not found")
        void syncDeal_NotFound_Returns404() throws Exception {
            when(dealService.syncWithSalesforce("missing"))
                    .thenThrow(new DealNotFoundException("Deal not found with id: missing"));

            mockMvc.perform(post("/api/v1/deals/missing/sync"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/deals/batch-sync
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/deals/batch-sync")
    class BatchSync {

        @Test
        @DisplayName("batch-syncs deals and returns 202 Accepted")
        void batchSync_ValidIds_Returns202() throws Exception {
            List<String> ids = List.of("deal-001", "deal-002");
            doNothing().when(dealService).batchSyncWithSalesforce(ids);

            mockMvc.perform(post("/api/v1/deals/batch-sync")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ids)))
                    .andExpect(status().isAccepted());

            verify(dealService).batchSyncWithSalesforce(ids);
        }
    }
}
