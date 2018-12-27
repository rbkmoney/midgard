package com.rbkmoney.midgard.base.clearing.services;

import com.rbkmoney.midgard.base.clearing.handlers.MigrationDataHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    private final MigrationDataHandler migrationDataHandler;

    @Override
    @Scheduled(fixedDelayString = "${migration.delay}")
    public void process() {
        log.debug("Migration get started!");

        //TODO: реализовать метод миграции данных из feed в midgard
        migrationDataHandler.handle();

        log.error("Procedure of migration is not realised yet!");
        log.debug("Data migration finished!");
    }

}
