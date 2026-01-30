package com.aciworldwide.dealdesk.model.tcv;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.aciworldwide.dealdesk.config.ApplicationContextProvider;
import com.aciworldwide.dealdesk.service.SalesforceService;

class RepricingTriggersTest {

    private ApplicationContext applicationContext;
    private SalesforceService salesforceService;
    private ApplicationContextProvider applicationContextProvider;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        salesforceService = mock(SalesforceService.class);

        when(applicationContext.getBean(SalesforceService.class)).thenReturn(salesforceService);

        // Initialize ApplicationContextProvider with the mock context
        applicationContextProvider = new ApplicationContextProvider();
        applicationContextProvider.setApplicationContext(applicationContext);
    }

    @Test
    void syncToCPQ_ShouldInvokeSalesforceService() {
        // Arrange
        String quoteId = "QUOTE-123";
        RepricingTriggers triggers = new RepricingTriggers();

        // Act
        triggers.syncToCPQ(quoteId);

        // Assert
        verify(salesforceService).applyPriceRules(quoteId);
    }
}
