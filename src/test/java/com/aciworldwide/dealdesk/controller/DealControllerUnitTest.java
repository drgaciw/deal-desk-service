package com.aciworldwide.dealdesk.controller;

import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.mapper.DealMapper;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import com.aciworldwide.dealdesk.service.DealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealControllerUnitTest {

    @Mock
    private DealService dealService;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private DealController dealController;

    @Test
    void searchDeals_ShouldUsePagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 42);
        when(dealService.getAllDeals(pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        // When
        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(null, null, null, null, null, pageable);

        // Then
        verify(dealService).getAllDeals(pageable);
        assertPage(response, responseDTO, 42);
    }

    @Test
    void searchDeals_WithStatus_ShouldUsePagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        DealStatus status = DealStatus.DRAFT;
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 17);
        when(dealService.getDealsByStatus(status, pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        // When
        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(status, null, null, null, null, pageable);

        // Then
        verify(dealService).getDealsByStatus(status, pageable);
        assertPage(response, responseDTO, 17);
    }

    @Test
    void searchDeals_WithAccount_ShouldUsePagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 13);
        when(dealService.getDealsByAccount("account-1", pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(null, "account-1", null, null, null, pageable);

        verify(dealService).getDealsByAccount("account-1", pageable);
        assertPage(response, responseDTO, 13);
    }

    @Test
    void searchDeals_WithSalesRep_ShouldUsePagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 14);
        when(dealService.getDealsBySalesRep("rep-1", pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(null, null, "rep-1", null, null, pageable);

        verify(dealService).getDealsBySalesRep("rep-1", pageable);
        assertPage(response, responseDTO, 14);
    }

    @Test
    void searchDeals_WithMinimumValue_ShouldUsePagination() {
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minValue = new BigDecimal("1000.00");
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 15);
        when(dealService.getHighValueDeals(minValue, DealStatus.APPROVED, pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(null, null, null, minValue, null, pageable);

        verify(dealService).getHighValueDeals(minValue, DealStatus.APPROVED, pageable);
        assertPage(response, responseDTO, 15);
    }

    @Test
    void searchDeals_WithSinceDate_ShouldUsePagination() {
        Pageable pageable = PageRequest.of(0, 10);
        ZonedDateTime since = ZonedDateTime.parse("2026-05-01T00:00:00Z");
        Deal deal = new Deal();
        DealResponseDTO responseDTO = new DealResponseDTO();
        Page<Deal> page = new PageImpl<>(List.of(deal), pageable, 16);
        when(dealService.getRecentDeals(since, List.of(DealStatus.values()), pageable)).thenReturn(page);
        when(dealMapper.toResponseDTO(deal)).thenReturn(responseDTO);

        ResponseEntity<Page<DealResponseDTO>> response = dealController.searchDeals(null, null, null, null, since, pageable);

        verify(dealService).getRecentDeals(since, List.of(DealStatus.values()), pageable);
        assertPage(response, responseDTO, 16);
    }

    private void assertPage(ResponseEntity<Page<DealResponseDTO>> response, DealResponseDTO responseDTO, long totalElements) {
        Page<DealResponseDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(0, body.getNumber());
        assertEquals(10, body.getSize());
        assertEquals(totalElements, body.getTotalElements());
        assertEquals(List.of(responseDTO), body.getContent());
    }
}
