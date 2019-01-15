package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.service.clearing.importers.Importer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Сервис отвечающий за миграцию данных из внешней системы в хранилище клирингового сервиса
 *
 * Примечание. В текущей реализации данные будут загружаться из BM аналогично newway
 *             За это отвечает пакет load, который по сути является клоном части кода newway. Он получает
 *             данные из BM и заливает их в схему feed рядом со схемой midgard, которая хранит обработанную
 *             информацию готовую к передаче в адаптер. Раз в час (а так же перед выполнением клиринга)
 *             будет запускаться задача миграции данных из feed в midgard.
 *             В перспективе необходимость пакета load отпадент, так как все что нужно будет в kafka и тогда нужно
 *             будет немного изменить importer
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MigrationDataService implements GenericService {

    //TODO: по-хорошему нужно вынести в отдельный класс получения блокировки по имени, но каждый случай
    //      использования блокировки нужно отдельно продумать
    private final static ReentrantLock lock = new ReentrantLock();
    //TODO: В данном случае можно и нужно запускать импортеры в параллельном режиме, но получение
    //      инстанса далеко не факт, что должно быть здесь
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final List<Importer> importers;

    @Override
    @Scheduled(fixedDelayString = "${import.migration.delay}")
    public void process() {
        log.debug("Migration get started!");

        if (lock.tryLock()) {
            try {
                lock.lock();
                log.debug("Migration data get started");

                //TODO: сделать импорты для конкретных провайдеров, чтобы не тащить лишнее
                runImporters(importers);

                log.debug("Migration data finished");
            } finally {
                lock.unlock();
            }
        } else {
            log.debug("Migration data has been running...");
        }

        log.debug("Data migration finished!");
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

}
