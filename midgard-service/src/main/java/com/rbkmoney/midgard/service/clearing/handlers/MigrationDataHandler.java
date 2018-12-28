package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.data.enums.HandlerType;
import com.rbkmoney.midgard.service.clearing.importers.Importer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class MigrationDataHandler implements Handler {

    //TODO: по-хорошему нужно вынести в отдельный класс получения блокировки по имени, но каждый случай
    //      использования блокировки нужно отдельно продумать
    private final static ReentrantLock lock = new ReentrantLock();
    //TODO: В данном случае можно и нужно запускать импортеры в параллельном режиме, но получение
    //      инстанса далеко не факт, что должно быть здесь
    private final ExecutorService executor = Executors.newCachedThreadPool();;

    private final List<Importer> importers;

    @Override
    public void handle() {
        if (lock.tryLock()) {
            try {
                lock.lock();
                log.debug("Migration data get started");

                runImporters(importers);

                log.debug("Migration data finished");
            } finally {
                lock.unlock();
            }
        } else {
            log.debug("Migration data has been running...");
        }
    }

    private void runImporters(List<Importer> importers) {
        List<Future<?>> importTasks = importers.stream()
                .map(importer -> executor.submit(importer::getData))
                .collect(Collectors.toList());
        try {
            for (Future<?> task : importTasks) {
                task.get();
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException was received during the migration", e);
            //TODO: продумать корректную обработку
        } catch (ExecutionException e) {
            log.error("ExecutionException was received during the migration", e);
        }
    }

    @Override
    public boolean isInstance(HandlerType handler) {
        return HandlerType.MIGRATE_DATA == handler;
    }

}
