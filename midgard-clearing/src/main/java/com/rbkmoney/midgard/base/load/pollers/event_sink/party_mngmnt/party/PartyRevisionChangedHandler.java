package com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.party;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.PartyRevisionChanged;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.base.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.base.load.DAO.party.iface.*;
import com.rbkmoney.midgard.base.load.DAO.party.iface.*;
import com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.ContractAdjustment;
import org.jooq.generated.feed.tables.pojos.Party;
import org.jooq.generated.feed.tables.pojos.PayoutTool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class PartyRevisionChangedHandler extends AbstractPartyManagementHandler {

    private final PartyDao partyDao;
    private final ContractDao contractDao;
    private final ContractAdjustmentDao contractAdjustmentDao;
    private final PayoutToolDao payoutToolDao;
    private final ContractorDao contractorDao;
    private final ShopDao shopDao;

    private final Filter filter;

    public PartyRevisionChangedHandler(PartyDao partyDao,
                                       ContractDao contractDao,
                                       ContractAdjustmentDao contractAdjustmentDao,
                                       PayoutToolDao payoutToolDao,
                                       ContractorDao contractorDao,
                                       ShopDao shopDao) {
        this.partyDao = partyDao;
        this.contractDao = contractDao;
        this.contractAdjustmentDao = contractAdjustmentDao;
        this.payoutToolDao = payoutToolDao;
        this.contractorDao = contractorDao;
        this.shopDao = shopDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "revision_changed",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        PartyRevisionChanged partyRevisionChanged = change.getRevisionChanged();
        String partyId = event.getSource().getPartyId();
        log.info("Start partySource revision changed handling, eventId={}, partyId={}", eventId, partyId);
        Party partySource = partyDao.get(partyId);
        if (partySource == null) {
            throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
        }
        partySource.setId(null);
        partySource.setWtime(null);
        partySource.setEventId(eventId);
        partySource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        long revision = partyRevisionChanged.getRevision();
        partySource.setRevision(revision);
        partySource.setRevisionChangedAt(TypeUtil.stringToLocalDateTime(partyRevisionChanged.getTimestamp()));
        partyDao.updateNotCurrent(partyId);
        partyDao.save(partySource);
        updateContractorsRevision(event, partyId, revision);
        updateContractsRevision(event, partyId, revision);
        updateShopsRevision(event, partyId, revision);
        log.info("Party revision changed has been saved, eventId={}, partyId={}", eventId, partyId);
    }

    private void updateShopsRevision(Event event, String partyId, long revision) {
        shopDao.getByPartyId(partyId).forEach(shopSource -> {
            String shopId = shopSource.getShopId();
            shopSource.setId(null);
            shopSource.setWtime(null);
            shopSource.setEventId(event.getId());
            shopSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            shopSource.setRevision(revision);
            shopDao.updateNotCurrent(partyId, shopId);
            shopDao.save(shopSource);
            log.info("Shop revision has been saved, eventId={}, partyId={}, shopId={}", event.getId(), partyId, shopId);
        });
    }

    private void updateContractorsRevision(Event event, String partyId, long revision) {
        contractorDao.getByPartyId(partyId).forEach(contractorSource -> {
            String contractorId = contractorSource.getContractorId();
            contractorSource.setId(null);
            contractorSource.setWtime(null);
            contractorSource.setEventId(event.getId());
            contractorSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            contractorSource.setRevision(revision);
            contractorDao.updateNotCurrent(partyId, contractorId);
            contractorDao.save(contractorSource);
            log.info("Contractor revision has been saved, eventId={}, partyId={}, contractorId={}",
                    event.getId(), partyId, contractorId);
        });
    }

    private void updateContractsRevision(Event event, String partyId, long revision) {
        contractDao.getByPartyId(partyId).forEach(contractSource -> {
            Long contractSourceId = contractSource.getId();
            String contractId = contractSource.getContractId();
            contractSource.setId(null);
            contractSource.setWtime(null);
            contractSource.setEventId(event.getId());
            contractSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            contractSource.setRevision(revision);
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
            log.info("Contract revision has been saved, eventId={}, partyId={}, contractId={}",
                    event.getId(), partyId, contractId);
        });
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
