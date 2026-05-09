package com.aciworldwide.dealdesk.model.tcv;

import com.aciworldwide.dealdesk.config.ApplicationContextProvider;
import com.aciworldwide.dealdesk.service.SalesforceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepricingTriggersTest {

    private ApplicationContext mockContext;
    private SalesforceService mockSalesforceService;
    private RepricingTriggers repricingTriggers;

    @BeforeEach
    void setUp() {
        mockContext = mock(ApplicationContext.class);
        mockSalesforceService = mock(SalesforceService.class);

        ApplicationContextProvider provider = new ApplicationContextProvider();
        provider.setApplicationContext(mockContext);

        when(mockContext.getBean(SalesforceService.class)).thenReturn(mockSalesforceService);

        repricingTriggers = new RepricingTriggers();
    }

    @Test
    void testValidateWithCPQ() {
        String quoteId = "Q-12345";
        repricingTriggers.validateWithCPQ(quoteId);
        verify(mockSalesforceService).evaluatePriceRules(quoteId);
    }

    @Test
    void testSyncToCPQSuccess() {
        String quoteId = "Q-12345";
        repricingTriggers.syncToCPQ(quoteId);

        verify(mockSalesforceService).applyPriceRules(quoteId);
        assertTrue(repricingTriggers.isSyncedWithCPQ());
        assertNotNull(repricingTriggers.getLastSyncTimestamp());
        assertNull(repricingTriggers.getLastSyncError());
    }

    @Test
    void testSyncToCPQFailure() {
        String quoteId = "Q-12345";
        String errorMessage = "Salesforce error";
        doThrow(new RuntimeException(errorMessage)).when(mockSalesforceService).applyPriceRules(quoteId);

        assertThrows(RuntimeException.class, () -> repricingTriggers.syncToCPQ(quoteId));

        verify(mockSalesforceService).applyPriceRules(quoteId);
        assertFalse(repricingTriggers.isSyncedWithCPQ());
        assertNotNull(repricingTriggers.getLastSyncTimestamp());
        assertEquals(errorMessage, repricingTriggers.getLastSyncError());
    }
}
