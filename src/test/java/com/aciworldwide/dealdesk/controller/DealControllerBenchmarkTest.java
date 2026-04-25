package com.aciworldwide.dealdesk.controller;

import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.mapper.DealMapper;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.service.DealService;
import com.aciworldwide.dealdesk.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DealControllerBenchmarkTest {

    @Mock
    private DealService dealService;

    @Mock
    private DealMapper dealMapper;

    @InjectMocks
    private DealController dealController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(dealController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void benchmarkSearchDeals_Paged() throws Exception {
        int pageSize = 20;
        List<Deal> deals = TestDataFactory.createDeals(pageSize);

        when(dealService.getAllDeals(any(Pageable.class))).thenReturn(new PageImpl<>(deals));
        when(dealMapper.toResponseDTO(any(Deal.class))).thenReturn(new DealResponseDTO());

        // Warmup
        mockMvc.perform(get("/api/v1/deals")
                        .param("page", "0")
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk());

        long startTime = System.currentTimeMillis();
        // We simulate a high load by calling it multiple times or just measuring one call latency?
        // The previous test measured one call with 5000 items.
        // Now one call returns 20 items. It should be instant.
        // To make it comparable, we can just show that fetching one page is extremely fast.

        mockMvc.perform(get("/api/v1/deals")
                        .param("page", "0")
                        .param("size", String.valueOf(pageSize)))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();

        System.out.println("Search Deals (Paged 20 items) took: " + (endTime - startTime) + " ms");
    }
}
