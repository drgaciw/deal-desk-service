package com.aciworldwide.dealdesk.model.tcv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.aciworldwide.dealdesk.config.ApplicationContextProvider;
import com.aciworldwide.dealdesk.service.SalesforceService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepricingTriggersTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private SalesforceService salesforceService;

    private ApplicationContextProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ApplicationContextProvider();
        provider.setApplicationContext(applicationContext);
        when(applicationContext.getBean(SalesforceService.class)).thenReturn(salesforceService);
    }

    @Test
    void syncToCPQ_shouldCallApplyPriceRules() {
        RepricingTriggers triggers = new RepricingTriggers();
        String quoteId = "Q-12345";

        triggers.syncToCPQ(quoteId);

        verify(salesforceService).applyPriceRules(quoteId);
    }
}
