package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.contractor;

import com.rbkmoney.damsel.domain.PartyContractor;
import com.rbkmoney.damsel.payment_processing.ContractorEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.party.iface.ContractorDao;
import com.rbkmoney.midgard.service.load.dao.party.iface.PartyDao;
import com.rbkmoney.midgard.service.load.utils.ContractorUtil;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Contractor;
import org.jooq.generated.feed.tables.pojos.Party;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ContractorCreatedHandler extends AbstractClaimChangedHandler {

    private final ContractorDao contractorDao;

    private final PartyDao partyDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetContractorEffect() && e.getContractorEffect().getEffect().isSetCreated()).forEach(e -> {
            ContractorEffectUnit contractorEffect = e.getContractorEffect();
            PartyContractor partyContractor = contractorEffect.getEffect().getCreated();
            com.rbkmoney.damsel.domain.Contractor contractorCreated = partyContractor.getContractor();
            String contractorId = contractorEffect.getId();
            String partyId = event.getSource().getPartyId();
            log.info("Start contractor created handling, eventId={}, partyId={}, contractorId={}",
                    eventId, partyId, contractorId);
            Party partySource = partyDao.get(partyId);
            if (partySource == null) {
                // TODO: исправить после того как прольется БД
                log.error("Party not found, partyId='{}'", partyId);
                return;
                //throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
            }
            Contractor contractor = ContractorUtil.convertContractor(eventId, event.getCreatedAt(), partyId, contractorCreated, contractorId);
            contractor.setIdentificationalLevel(partyContractor.getStatus().name());
            contractorDao.save(contractor);
            log.info("Contract contractor has been saved, eventId={}, partyId={}, contractorId={}",
                    eventId, partyId, contractorId);

        });
    }


}
