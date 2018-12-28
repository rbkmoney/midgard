package com.rbkmoney.midgard.service.clearing.importers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.midgard.service.clearing.data.TerminalOptionalJson;
import com.rbkmoney.midgard.service.clearing.data.enums.ImporterType;
import com.rbkmoney.midgard.service.clearing.helpers.PaymentHelper;
import com.rbkmoney.midgard.service.clearing.helpers.TerminalHelper;
import com.rbkmoney.midgard.service.clearing.helpers.TransactionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Payment;
import org.jooq.generated.feed.tables.pojos.Terminal;
import org.jooq.generated.midgard.enums.TransactionClearingState;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionImporter implements Importer {

    private final TransactionHelper transactionHelper;

    private final PaymentHelper paymentHelper;

    private final TerminalHelper terminalHelper;

    @Value("${import.trx-pool-size}")
    private int poolSize;

    @Override
    public void getData() {
        long eventId = transactionHelper.getMaxTransactionEventId();
        log.info("Transaction data import will start with event id {}", eventId);
        List<Payment> payments;
        do {
            payments = paymentHelper.getPayments(eventId, poolSize);
            try {
                for (Payment payment : payments) {
                    ClearingTransaction transaction = transformTransaction(payment);
                    transactionHelper.saveTransaction(transaction);
                }
            } catch (IOException ex) {
                log.error("Error was detected: {}", ex);
                break;
            }
        } while(payments.size() == poolSize);
        log.info("Transaction data import have finished");
    }

    private ClearingTransaction transformTransaction(Payment payment) throws IOException {
        ClearingTransaction trx = new ClearingTransaction();
        trx.setEventId(payment.getEventId());
        trx.setInvoiceId(payment.getInvoiceId());
        trx.setDocId(payment.getInvoiceId());
        trx.setProviderId(payment.getRouteProviderId());
        //TODO: что то придумать с tran_id
        trx.setTransactionId(payment.getInvoiceId() + "_" + payment.getPaymentId());
        trx.setTransactionDate(payment.getCreatedAt());
        trx.setTransactionAmount(payment.getAmount());
        trx.setTransactionCurrency(payment.getCurrencyCode());
        //TODO: подумать над тем в какой тип все таки установить транзакцию
        trx.setTransactionClearingState(TransactionClearingState.CREATED);
        trx.setPartyId(payment.getPartyId());
        trx.setShopId(payment.getShopId());

        Terminal terminal = terminalHelper.getTerminal(payment.getRouteTerminalId());
        String optionsJson = terminal.getOptionsJson();
        ObjectMapper mapper = new ObjectMapper();
        TerminalOptionalJson termOptionalJson = mapper.readValue(optionsJson, TerminalOptionalJson.class);
        trx.setMerchantId(termOptionalJson.getMerchantId());
        trx.setTerminalId(termOptionalJson.getTerminalId());

        trx.setPayerBankCardToken(payment.getPayerBankCardToken());
        trx.setPayerBankCardPaymentSystem(payment.getPayerBankCardPaymentSystem());
        trx.setPayerBankCardBin(payment.getPayerBankCardBin());
        trx.setPayerBankCardMaskedPan(payment.getPayerBankCardMaskedPan());
        trx.setPayerBankCardTokenProvider(payment.getPayerBankCardTokenProvider());
        trx.setFee(payment.getFee());
        trx.setExternalFee(payment.getExternalFee());
        trx.setProviderFee(payment.getProviderFee());
        trx.setExtra(payment.getSessionPayloadTransactionBoundTrxExtraJson());
        return trx;
    }

    @Override
    public boolean isInstance(ImporterType type) {
        return ImporterType.TRANSACTION == type;
    }

}
