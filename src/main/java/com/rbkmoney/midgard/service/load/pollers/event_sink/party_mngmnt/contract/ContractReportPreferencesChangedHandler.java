package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.contract;

import com.rbkmoney.damsel.domain.ReportPreferences;
import com.rbkmoney.damsel.payment_processing.ContractEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.party.iface.ContractAdjustmentDao;
import com.rbkmoney.midgard.service.load.dao.party.iface.ContractDao;
import com.rbkmoney.midgard.service.load.dao.party.iface.PayoutToolDao;
import com.rbkmoney.midgard.service.load.utils.ContractUtil;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Contract;
import org.jooq.generated.feed.tables.pojos.ContractAdjustment;
import org.jooq.generated.feed.tables.pojos.PayoutTool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ContractReportPreferencesChangedHandler extends AbstractClaimChangedHandler {

    private final ContractDao contractDao;

    private final ContractAdjustmentDao contractAdjustmentDao;

    private final PayoutToolDao payoutToolDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetContractEffect() && e.getContractEffect().getEffect().isSetReportPreferencesChanged()).forEach(e -> {
            ContractEffectUnit contractEffectUnit = e.getContractEffect();
            ReportPreferences reportPreferencesChanged = contractEffectUnit.getEffect().getReportPreferencesChanged();
            String contractId = contractEffectUnit.getContractId();
            String partyId = event.getSource().getPartyId();
            log.info("Start contract report preferences changed handling, eventId={}, partyId={}, contractId={}",
                    eventId, partyId, contractId);
            Contract contractSource = contractDao.get(partyId, contractId);
            if (contractSource == null) {
                // TODO: исправить после того как прольется БД
                log.error("Contract not found, contractId='{}'", contractId);
                return;
                //throw new NotFoundException(String.format("Contract not found, contractId='%s'", contractId));
            }
            Long contractSourceId = contractSource.getId();
            contractSource.setId(null);
            contractSource.setWtime(null);
            contractSource.setEventId(eventId);
            contractSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            if (reportPreferencesChanged != null && reportPreferencesChanged.isSetServiceAcceptanceActPreferences()) {
                ContractUtil.fillReportPreferences(contractSource, reportPreferencesChanged.getServiceAcceptanceActPreferences());
            } else {
                ContractUtil.setNullReportPreferences(contractSource);
            }
            contractDao.updateNotCurrent(partyId, contractId);
            long cntrctId = contractDao.save(contractSource);

            List<ContractAdjustment> adjustments = contractAdjustmentDao.getByCntrctId(contractSourceId);
            adjustments.forEach(a -> {
                a.setId(null);
                a.setCntrctId(cntrctId);
            });
            contractAdjustmentDao.save(adjustments);

            List<PayoutTool> payoutTools = payoutToolDao.getByCntrctId(contractSourceId);
            payoutTools.forEach(pt -> {
                pt.setId(null);
                pt.setCntrctId(cntrctId);
            });
            payoutToolDao.save(payoutTools);

            log.info("Contract report preferences has been saved, eventId={}, partyId={}, contractId={}",
                    eventId, partyId, contractId);
        });
    }
}
