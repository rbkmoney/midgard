package com.rbkmoney.midgard.load.utils;

import com.rbkmoney.damsel.domain.FinalCashFlowAccount;
import com.rbkmoney.damsel.domain.FinalCashFlowPosting;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import org.jooq.generated.feed.enums.AdjustmentCashFlowType;
import org.jooq.generated.feed.enums.CashFlowAccount;
import org.jooq.generated.feed.enums.PaymentChangeType;
import org.jooq.generated.feed.tables.pojos.CashFlow;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CashFlowUtil {

    public static CashFlowAccount getCashFlowAccountType(FinalCashFlowAccount cfa) {
        CashFlowAccount sourceAccountType =
                TypeUtil.toEnumField(cfa.getAccountType().getSetField().getFieldName(), CashFlowAccount.class);
        if (sourceAccountType == null) {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
        return sourceAccountType;
    }

    public static String getCashFlowAccountTypeValue(FinalCashFlowAccount cfa) {
        if (cfa.getAccountType().isSetMerchant()) {
            return cfa.getAccountType().getMerchant().name();
        } else if (cfa.getAccountType().isSetProvider()) {
            return cfa.getAccountType().getProvider().name();
        } else if (cfa.getAccountType().isSetSystem()) {
            return cfa.getAccountType().getSystem().name();
        } else if (cfa.getAccountType().isSetExternal()) {
            return cfa.getAccountType().getExternal().name();
        } else if (cfa.getAccountType().isSetWallet()) {
            return cfa.getAccountType().getWallet().name();
        } else {
            throw new IllegalArgumentException("Illegal cash flow account type: " + cfa.getAccountType());
        }
    }

    public static String getCashFlowAccountTypeValue(com.rbkmoney.fistful.cashflow.FinalCashFlowAccount cfa) {
        if (cfa.getAccountType().isSetMerchant()) {
            return cfa.getAccountType().getMerchant().name();
        } else if (cfa.getAccountType().isSetProvider()) {
            return cfa.getAccountType().getProvider().name();
        } else if (cfa.getAccountType().isSetSystem()) {
            return cfa.getAccountType().getSystem().name();
        } else if (cfa.getAccountType().isSetExternal()) {
            return cfa.getAccountType().getExternal().name();
        } else if (cfa.getAccountType().isSetWallet()) {
            return cfa.getAccountType().getWallet().name();
        } else {
            throw new IllegalArgumentException("Illegal fistful cash flow account type: " + cfa.getAccountType());
        }
    }

    public static List<CashFlow> convertCashFlows(List<FinalCashFlowPosting> cashFlowPostings,
                                                  long objId,
                                                  PaymentChangeType paymentchangetype) {
        return convertCashFlows(cashFlowPostings, objId, paymentchangetype, null);
    }

    public static List<CashFlow> convertCashFlows(List<FinalCashFlowPosting> cashFlowPostings,
                                                  long objId,
                                                  PaymentChangeType paymentchangetype,
                                                  AdjustmentCashFlowType adjustmentcashflowtype) {
        return cashFlowPostings.stream().map(cf -> {
            CashFlow pcf = new CashFlow();
            pcf.setObjId(objId);
            pcf.setObjType(paymentchangetype);
            pcf.setAdjFlowType(adjustmentcashflowtype);
            pcf.setSourceAccountType(CashFlowUtil.getCashFlowAccountType(cf.getSource()));
            pcf.setSourceAccountTypeValue(getCashFlowAccountTypeValue(cf.getSource()));
            pcf.setSourceAccountId(cf.getSource().getAccountId());
            pcf.setDestinationAccountType(CashFlowUtil.getCashFlowAccountType(cf.getDestination()));
            pcf.setDestinationAccountTypeValue(getCashFlowAccountTypeValue(cf.getDestination()));
            pcf.setDestinationAccountId(cf.getDestination().getAccountId());
            pcf.setAmount(cf.getVolume().getAmount());
            pcf.setCurrencyCode(cf.getVolume().getCurrency().getSymbolicCode());
            pcf.setDetails(cf.getDetails());
            return pcf;
        }).collect(Collectors.toList());
    }

    public static long getFistfulFee(List<com.rbkmoney.fistful.cashflow.FinalCashFlowPosting> postings) {
        return getFistfulAmount(
                postings,
                posting -> posting.getSource().getAccountType().isSetWallet()
                        && posting.getDestination().getAccountType().isSetSystem()
        );
    }

    public static long getFistfulProviderFee(List<com.rbkmoney.fistful.cashflow.FinalCashFlowPosting> postings) {
        return getFistfulAmount(
                postings,
                posting -> posting.getSource().getAccountType().isSetSystem()
                        && posting.getDestination().getAccountType().isSetProvider()
        );
    }

    public static long getFistfulAmount(
            List<com.rbkmoney.fistful.cashflow.FinalCashFlowPosting> postings,
            Predicate<com.rbkmoney.fistful.cashflow.FinalCashFlowPosting> filter
    ) {
        return postings.stream()
                .filter(filter)
                .map(posting -> posting.getVolume().getAmount())
                .reduce(0L, Long::sum);
    }

}
