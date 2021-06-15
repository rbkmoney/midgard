package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckTransactionType {

    private final List<ClearingAdapter> adapters;

    private Optional<ClearingAdapter> extractClearingAdapter(int providerId) {
        return adapters.stream()
                .filter(adapter -> adapter.getAdapterId() == providerId)
                .findFirst();
    }

    public boolean isTypeTransactionToSkipped(
            InvoicePayment invoicePayment,
            String invoiceId,
            Integer changeId,
            long sequenceId,
            int providerId
    ) {
        Optional<ClearingAdapter> clearingAdapter = extractClearingAdapter(providerId);
        return clearingAdapter
                .filter(adapter -> isTypeTransactionToSkipped(
                        adapter, invoicePayment, invoiceId, changeId, sequenceId))
                .isPresent();
    }

    public boolean isTypeTransactionToSkipped(
            ClearingAdapter clearingAdapter,
            InvoicePayment invoicePayment,
            String invoiceId,
            Integer changeId,
            long sequenceId
    ) {
        TransactionInfo transactionInfo = extractTransactionInfo(invoicePayment, invoiceId, changeId, sequenceId);
        if (transactionInfo != null) {
            Map<String, String> extra = transactionInfo.getExtra();
            Optional<String[]> excludeTransactionType = extractExcludeTransactionType(clearingAdapter);
            if (excludeTransactionType.isEmpty()) {
                return false;
            }
            Optional<String> transactionTypes = Arrays.stream(excludeTransactionType.get())
                    .map(String::toLowerCase)
                    .filter(value -> extra.containsValue(value.toLowerCase()))
                    .findFirst();
            return transactionTypes.isPresent();
        }
        return false;
    }

    @NotNull
    private Optional<String[]> extractExcludeTransactionType(ClearingAdapter clearingAdapter) {
        return Optional.ofNullable(clearingAdapter.getExcludeTransactions())
                .map(ClearingServiceProperties.ExcludeTransactions::getTypes);
    }

    private TransactionInfo extractTransactionInfo(
            InvoicePayment invoicePayment,
            String invoiceId,
            Integer changeId,
            long sequenceId
    ) {
        InvoicePaymentSession paymentSession = MappingUtils.extractPaymentSession(
                invoicePayment, invoiceId, changeId, sequenceId
        );
        return paymentSession.getTransactionInfo();
    }
}
