package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.service.clearing.services.ClearingEventService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class ClearingEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClearingEventService clearingEventService;

    private ClearingEvent clearingEvent;

    @Before
    public void before() {
        ClearingEvent clearingEvent = new ClearingEvent();
        clearingEvent.setEventId(1);
        clearingEvent.setProviderId(100);
    }

    @Test
    public void clearingEventIntegrationTest() {

        //TODO: write integration tests

    }

    @After
    public void after() throws IOException {
        destroy();
    }

}
