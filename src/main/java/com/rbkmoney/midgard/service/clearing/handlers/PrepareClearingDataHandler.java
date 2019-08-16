package com.rbkmoney.midgard.service.clearing.handlers;

import com.rbkmoney.midgard.service.clearing.dao.clearing_info.ClearingEventInfoDao;
import com.rbkmoney.midgard.service.clearing.data.ClearingProcessingEvent;
import com.rbkmoney.midgard.service.clearing.exception.PreparingDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.jooq.generated.midgard.enums.ClearingEventStatus.PREPARING_DATA_FAULT;

@Slf4j
@RequiredArgsConstructor
@Component
public class PrepareClearingDataHandler implements Handler<ClearingProcessingEvent> {

    private final ClearingEventInfoDao clearingEventInfoDao;

    @Override
    @Transactional
    public void handle(ClearingProcessingEvent clearingEvent) throws Exception {
        int providerId = clearingEvent.getClearingAdapter().getAdapterId();
        Long clearingId = clearingEvent.getClearingId();
        try {
            // Подготовка транзакций для клиринга
            log.info("Preparing event for provider id {} started", providerId);

            clearingEventInfoDao.prepareTransactionData(clearingId, providerId);

            log.info("Preparing event {} for provider id {} was finished", clearingId, providerId);
        } catch (PreparingDataException ex) {
            log.error("Error received while processing data", ex);
            clearingEventInfoDao.updateClearingStatus(clearingId, PREPARING_DATA_FAULT);
            throw new Exception(ex);
        }
    }

}
