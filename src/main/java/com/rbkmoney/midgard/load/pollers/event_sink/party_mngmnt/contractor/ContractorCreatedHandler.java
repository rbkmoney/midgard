package com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.contractor;

import com.rbkmoney.damsel.domain.PartyContractor;
import com.rbkmoney.damsel.payment_processing.ContractorEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.midgard.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.load.DAO.party.iface.ContractorDao;
import com.rbkmoney.midgard.load.DAO.party.iface.PartyDao;
import com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import com.rbkmoney.midgard.load.utils.ContractorUtil;
import org.jooq.generated.feed.tables.pojos.Contractor;
import org.jooq.generated.feed.tables.pojos.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContractorCreatedHandler extends AbstractClaimChangedHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ContractorDao contractorDao;

    private final PartyDao partyDao;

    public ContractorCreatedHandler(ContractorDao contractorDao, PartyDao partyDao) {
        this.contractorDao = contractorDao;
        this.partyDao = partyDao;
    }

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
                throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
            }
            Contractor contractor = ContractorUtil.convertContractor(eventId, event.getCreatedAt(),
                    partyId, partySource.getRevision(), contractorCreated, contractorId);
            contractor.setIdentificationalLevel(partyContractor.getStatus().name());
            contractorDao.save(contractor);
            log.info("Contract contractor has been saved, eventId={}, partyId={}, contractorId={}",
                    eventId, partyId, contractorId);

        });
    }


}
