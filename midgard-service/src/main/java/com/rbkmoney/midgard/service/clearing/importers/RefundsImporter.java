package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.helpers.refund.RefundHelper;
import com.rbkmoney.midgard.service.clearing.helpers.transaction.TransactionHelper;
import com.rbkmoney.midgard.service.clearing.utils.MappingUtils;
import com.rbkmoney.midgard.service.config.props.AdapterProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefundsImporter implements Importer {

    private final TransactionHelper transactionHelper;

    private final RefundHelper refundHelper;

    private final List<AdapterProps> adaptersProps;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = transactionHelper.getLastTransactionEventId();
        log.info("Refund data import will start with event id {}", eventId);

        List<Integer> providerIds = adaptersProps.stream()
                .map(adapterProps -> adapterProps.getProviderId())
                .collect(Collectors.toList());

        int obtainRufundsSize;
        do {
            obtainRufundsSize = pollRefunds(eventId, providerIds);
        } while(obtainRufundsSize == poolSize);
        log.info("Transaction data import have finished");
    }

    private int pollRefunds(long eventId, List<Integer> providerIds) {
        List<Refund> refunds = refundHelper.getRefunds(eventId, providerIds, poolSize);
        for (Refund refund : refunds) {
            ClearingRefund clearingRefund = MappingUtils.transformRefund(refund);
            refundHelper.saveClearingRefund(clearingRefund);
        }
        return refunds.size();
    }

}
