package com.rbkmoney.midgard.service.check;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.payment_processing.InvoicePayment;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentSession;
import com.rbkmoney.midgard.config.props.ClearingServiceProperties;
import com.rbkmoney.midgard.data.ClearingAdapter;
import com.rbkmoney.midgard.utils.ClearingAdaptersUtils;
import com.rbkmoney.midgard.utils.MappingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationCheckingService {

    public boolean isOperationForSkip(List<ClearingAdapter> adapters,
                                      InvoicePayment invoicePayment,
                                      String invoiceId,
                                      Integer changeId,
                                      long sequenceId,
                                      int providerId) {
        ClearingAdapter clearingAdapter = ClearingAdaptersUtils.getClearingAdapter(adapters, providerId);
        Optional<List<String>> excludeOperationParams = extractExcludeOperationParams(clearingAdapter);

        TransactionInfo transactionInfo = extractTransactionInfo(invoicePayment, invoiceId, changeId, sequenceId);
        if (transactionInfo == null || excludeOperationParams.isEmpty()) {
            return false;
        }
        Map<String, String> extra = transactionInfo.getExtra();
        return excludeOperationParams.get().stream()
                .map(String::toLowerCase)
                .anyMatch(value -> extra.containsValue(value.toLowerCase()));
    }

    @NotNull
    private Optional<List<String>> extractExcludeOperationParams(ClearingAdapter clearingAdapter) {
        return Optional.ofNullable(clearingAdapter.getExcludeOperationParams())
                .map(ClearingServiceProperties.ExcludeOperationParams::getTypes);
    }

    private TransactionInfo extractTransactionInfo(
            InvoicePayment invoicePayment,
            String invoiceId,
            Integer changeId,
            long sequenceId) {
        InvoicePaymentSession paymentSession = MappingUtils.extractPaymentSession(
                invoicePayment, invoiceId, changeId, sequenceId
        );
        return paymentSession.getTransactionInfo();
    }
}
