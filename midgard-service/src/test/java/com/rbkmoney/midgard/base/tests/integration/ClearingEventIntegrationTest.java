package com.rbkmoney.midgard.base.tests.integration;

import com.rbkmoney.midgard.ClearingEvent;
import com.rbkmoney.midgard.service.clearing.services.ClearingEventService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ClearingEventIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClearingEventService clearingEventService;

    private ClearingEvent clearingEvent;

    @Test
    public void clearingEventIntegrationTest() throws InterruptedException {

        ClearingEvent clearingEvent = new ClearingEvent();
        clearingEvent.setEventId(1);
        clearingEvent.setProviderId(100);
        //TODO: write integration tests

        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();

            lock.tryLock(60L, TimeUnit.SECONDS);

        } finally {
            lock.unlock();
        }
    }

}
