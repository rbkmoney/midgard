package com.rbkmoney.midgard.service.clearing.services;

import com.rbkmoney.midgard.service.clearing.importers.Importer;
import com.rbkmoney.midgard.service.config.props.AdapterProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final List<Importer> importers;

    private final List<AdapterProps> adaptersProps;

    @Override
    @Scheduled(fixedDelayString = "${import.migration.delay}")
    public void process() {
        log.debug("Migration data get started");
        List<Integer> providerIds = adaptersProps.stream()
                .map(adapterProps -> adapterProps.getProviderId())
                .collect(Collectors.toList());

        try {
            for (Importer importer : importers) {
                while (importer.importData(providerIds));
            }
        } catch (Exception ex) {
            log.error("Error detected during data migration", ex);
        }

        log.debug("Data migration is finished!");
    }

}
