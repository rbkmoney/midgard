package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;
import com.rbkmoney.midgard.service.clearing.helpers.RefundHelper;
import com.rbkmoney.midgard.service.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Refund;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingRefund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RefundsImporter implements Importer {

    private final TransactionHelper transactionHelper;

    private final RefundHelper refundHelper;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = transactionHelper.getLastTransactionEventId();
        log.info("Transaction data import will start with event id {}", eventId);
        List<Refund> refunds;
        do {
            refunds = refundHelper.getRefund(eventId, poolSize);
            for (Refund refund : refunds) {
                ClearingRefund clearingRefund = transformRefund(refund);
                refundHelper.saveClearingRefund(clearingRefund);
            }
        } while(refunds.size() == poolSize);
        log.info("Transaction data import have finished");
    }

    private ClearingRefund transformRefund(Refund refund) {
        ClearingRefund clearingRefund = new ClearingRefund();
        clearingRefund.setEventId(refund.getEventId());
        clearingRefund.setInvoiceId(refund.getInvoiceId());
        clearingRefund.setPaymentId(refund.getPaymentId());
        clearingRefund.setPartyId(refund.getPartyId());
        clearingRefund.setShopId(refund.getShopId());
        clearingRefund.setCreatedAt(refund.getCreatedAt());
        clearingRefund.setAmount(refund.getAmount());
        clearingRefund.setCurrencyCode(refund.getCurrencyCode());
        clearingRefund.setReason(refund.getReason());
        clearingRefund.setDomainRevision(refund.getDomainRevision());
        clearingRefund.setClearingState(TransactionClearingState.READY);
        return clearingRefund;
    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.REFUND == type;
    }

}
