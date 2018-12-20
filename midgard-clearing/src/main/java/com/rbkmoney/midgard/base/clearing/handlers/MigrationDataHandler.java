package com.rbkmoney.midgard.base.clearing.handlers;

import com.rbkmoney.midgard.base.clearing.data.enums.HandlerType;
import com.rbkmoney.midgard.base.clearing.importers.Importer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/** Исполнитель для получения данных из внешней системы */
@Slf4j
@RequiredArgsConstructor
@Component
public class MigrationDataHandler implements Handler {

    /** Блокировка */
    private final static ReentrantLock lock = new ReentrantLock();
    //TODO: лауреат на премию "ПЕРЕДЕЛАТЬ"
    private final ExecutorService executor = Executors.newCachedThreadPool();;
    /** Список импортеров данных */
    private List<Importer> importers;

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
            log.debug("Migration data has been rinning...");
        }
    }

    /**
     * Запускает импортеры на выполнение
     *
     * Примечание: на данном этапе порядок импорта не важен (FK не проставлены), но в дальшейшем порядок может
     *             играть важную роль, поэтому с одной стороны запуск задач на импорт вынесен в отдельный метод,
     *             а в импортеры добавлен тип импортируемых данныъ
     *
     * @param importers список импортеров
     */
    private void runImporters(List<Importer> importers) {
        List<Future<?>> importTasks = new ArrayList<>();
        for (Importer importer : importers) {
            importTasks.add(executor.submit(() -> importer.getData()));
        }
        try {
            for (Future<?> task : importTasks) {
                task.get();
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException was received during the migration", e);
        } catch (ExecutionException e) {
            log.error("ExecutionException was received during the migration", e);
        }
    }

    @Override
    public boolean isInstance(HandlerType handler) {
        return HandlerType.MIGRATE_DATA == handler;
    }

}
