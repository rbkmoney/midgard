package com.rbkmoney.midgard.base.clearing.services;

import com.rbkmoney.midgard.base.clearing.handlers.MigrationDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

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

    /** Исполнитель миграции данных */
    private final MigrationDataHandler migrationDataHandler;
    /** Блокировка для процедуры миграции данных */
    private static ReentrantLock migrationLock = new ReentrantLock();

    @Override
    @Scheduled(fixedDelayString = "${migration.delay}")
    public void process() {
        //TODO: правильнее будет устанавливать блокировку на уровне конкретного импортера
        if (migrationLock.tryLock()) {
            try {
                migrationLock.lock();
                log.debug("Migration get started!");

                //TODO: реализовать метод миграции данных из feed в midgard
                migrationDataHandler.handle();

                log.error("Procedure of migration is not realised yet!");
            } catch (Exception ex) {
                log.error("Error was detected during data migration: ", ex);
            } finally {
                migrationLock.unlock();
            }
            log.debug("Data migration finished!");
        } else {
            log.info("Data migration have benn running...");
        }
    }

}
