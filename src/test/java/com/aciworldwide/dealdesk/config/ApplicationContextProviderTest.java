package com.aciworldwide.dealdesk.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationContextProviderTest {

    @Test
    void testGetBean() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        ApplicationContextProvider provider = new ApplicationContextProvider();
        provider.setApplicationContext(mockContext);

        String dummyBean = "dummy";
        when(mockContext.getBean(String.class)).thenReturn(dummyBean);

        assertEquals(dummyBean, ApplicationContextProvider.getBean(String.class));
    }
}
