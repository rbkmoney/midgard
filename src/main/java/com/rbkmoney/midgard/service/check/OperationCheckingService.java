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

    private final List<ClearingAdapter> adapters;

    public boolean isOperationForSkip(InvoicePayment invoicePayment {
        ClearingAdapter clearingAdapter = ClearingAdaptersUtils.getClearingAdapter(adapters,
                invoicePayment.getRoute().getProvider().getId());
        Optional<List<String>> excludeOperationParams = extractExcludeOperationParams(clearingAdapter);

        TransactionInfo transactionInfo = extractTransactionInfo(invoicePayment);
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

    private TransactionInfo extractTransactionInfo(InvoicePayment invoicePayment) {
        return MappingUtils.extractPaymentSession(invoicePayment).getTransactionInfo();
    }
}
