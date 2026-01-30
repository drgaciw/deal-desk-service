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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Page<Deal> page = new PageImpl<>(List.of(deal));
        when(dealService.getAllDeals(pageable)).thenReturn(page);
        when(dealMapper.toResponseDTOList(any())).thenReturn(List.of(new DealResponseDTO()));

        // When
        dealController.searchDeals(null, null, null, null, null, pageable);

        // Then
        verify(dealService).getAllDeals(pageable);
    }

    @Test
    void searchDeals_WithStatus_ShouldUsePagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        DealStatus status = DealStatus.DRAFT;
        Page<Deal> page = new PageImpl<>(List.of(new Deal()));
        when(dealService.getDealsByStatus(status, pageable)).thenReturn(page);
        when(dealMapper.toResponseDTOList(any())).thenReturn(List.of(new DealResponseDTO()));

        // When
        dealController.searchDeals(status, null, null, null, null, pageable);

        // Then
        verify(dealService).getDealsByStatus(status, pageable);
    }
}
