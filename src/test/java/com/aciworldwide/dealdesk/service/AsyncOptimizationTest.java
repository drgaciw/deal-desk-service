package com.aciworldwide.dealdesk.service;

import com.aciworldwide.dealdesk.controller.DealController;
import com.aciworldwide.dealdesk.mapper.DealMapper;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.repository.DealRepository;
import com.aciworldwide.dealdesk.rules.engine.PricingRuleEngine;
import com.aciworldwide.dealdesk.rules.service.DealStatusRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.DealValidationRuleExecutorService;
import com.aciworldwide.dealdesk.rules.service.TCVRuleExecutorService;
import com.aciworldwide.dealdesk.service.impl.DealServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;

@ExtendWith(SpringExtension.class)
@Import({DealServiceImpl.class, DealController.class})
public class AsyncOptimizationTest {

    @Autowired
    private DealRepository dealRepository;
    @Autowired
    private SalesforceService salesforceService;

    // Autowired mocks
    @Autowired
    private PricingRuleEngine pricingRuleEngine;
    @Autowired
    private TCVRuleExecutorService tcvRuleExecutorService;
    @Autowired
    private DealValidationRuleExecutorService dealValidationRuleExecutorService;
    @Autowired
    private DealStatusRuleExecutorService dealStatusRuleExecutorService;
    @Autowired
    private DealMapper dealMapper;

    @Autowired
    private DealController dealController;

    @TestConfiguration
    @EnableAsync
    static class Config {
        @Bean
        @Primary
        public DealRepository dealRepository() {
            return mock(DealRepository.class);
        }
        @Bean
        @Primary
        public SalesforceService salesforceService() {
            return mock(SalesforceService.class);
        }
        @Bean
        @Primary
        public PricingRuleEngine pricingRuleEngine() {
            return mock(PricingRuleEngine.class);
        }
        @Bean
        @Primary
        public TCVRuleExecutorService tcvRuleExecutorService() {
            return mock(TCVRuleExecutorService.class);
        }
        @Bean
        @Primary
        public DealValidationRuleExecutorService dealValidationRuleExecutorService() {
            return mock(DealValidationRuleExecutorService.class);
        }
        @Bean
        @Primary
        public DealStatusRuleExecutorService dealStatusRuleExecutorService() {
            return mock(DealStatusRuleExecutorService.class);
        }
        @Bean
        @Primary
        public DealMapper dealMapper() {
            return mock(DealMapper.class);
        }
    }

    @Test
    void testBatchSyncPerformance() {
        // Setup
        List<String> ids = Arrays.asList("1", "2", "3");
        List<Deal> deals = Arrays.asList(new Deal(), new Deal(), new Deal());

        when(dealRepository.findAllById(ids)).thenReturn(deals);

        // Simulate delay in Salesforce service
        doAnswer(invocation -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new Deal();
        }).when(salesforceService).syncDealToOpportunity(any());

        long start = System.currentTimeMillis();
        dealController.batchSyncWithSalesforce(ids);
        long end = System.currentTimeMillis();

        long duration = end - start;
        System.out.println("Execution Duration: " + duration + "ms");

        // Assert that the duration is short (Asynchronous behavior)
        assertThat(duration).isLessThan(200);

        // Verify that the operation completes eventually
        verify(salesforceService, timeout(2000).times(3)).syncDealToOpportunity(any());
    }
}
