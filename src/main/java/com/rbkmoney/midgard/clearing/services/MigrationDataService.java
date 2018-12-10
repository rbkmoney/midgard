package com.rbkmoney.midgard.clearing.services;

import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис отвечающий за миграцию данных из внешней системы в хранилище клирингового сервиса
 *
 * Примечание. В текущей реализации данные загружаться будут из BM аналогично newway
 *             За это отвечает пакет load, который по сути является клоном части кода newway. Он получает
 *             данные из BM и заливает их в схему feed рядом со схемой midgard, которая хранит обработанную
 *             информацию готовую к передаче в адаптер. Раз в час (а так же перед выполнением клиринга)
 *             будет запускаться задача миграции данных из feed в midgard.
 *             В перспективе необходимость пакета load отпадент, так как все что нужно будет в kafka и тогда нужно
 *             будет немного изменить importer
 */
@Service
@DependsOn("dbInitializer")
public class MigrationDataService implements GenericService {

    public MigrationDataService() {

    }

    @Override
    @Scheduled(fixedDelayString = "${migration.delay}")
    public void process() {
        //TODO: реализовать метод миграции данных из feed в midgard

    }

}
