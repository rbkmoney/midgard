package com.rbkmoney.midgard.service.load.services;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LockService {

    private final Map<String, ReentrantLock> locksMap = new ConcurrentHashMap<>();

    public boolean tryLock(String key) {
        return locksMap.containsKey(key);
    }

    public synchronized ReentrantLock getLock(String key) {
        if (tryLock(key)) {
            return locksMap.get(key);
        } else {
            ReentrantLock lock = new ReentrantLock();
            locksMap.put(key, lock);
            return lock;
        }
    }

    public synchronized void removeLock(String key) {
        if (tryLock(key)) {
            ReentrantLock lock = locksMap.get(key);
            if (lock.tryLock()) {
                locksMap.remove(key);
            } else {
                log.warn("Lock {} are used right now", key);
            }
        }
    }


}
