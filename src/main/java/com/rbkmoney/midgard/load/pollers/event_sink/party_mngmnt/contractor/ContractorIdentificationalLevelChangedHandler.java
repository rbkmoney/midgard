package com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.contractor;

import com.rbkmoney.damsel.domain.ContractorIdentificationLevel;
import com.rbkmoney.damsel.payment_processing.ContractorEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.midgard.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.load.DAO.party.iface.ContractorDao;
import com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import org.jooq.generated.feed.tables.pojos.Contractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContractorIdentificationalLevelChangedHandler extends AbstractClaimChangedHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ContractorDao contractorDao;

    public ContractorIdentificationalLevelChangedHandler(ContractorDao contractorDao) {
        this.contractorDao = contractorDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetContractorEffect()
                        && e.getContractorEffect().getEffect().isSetIdentificationLevelChanged()).forEach(e -> {
            ContractorEffectUnit contractorEffect = e.getContractorEffect();
            ContractorIdentificationLevel identificationLevelChanged = contractorEffect.getEffect().getIdentificationLevelChanged();
            String contractorId = contractorEffect.getId();
            String partyId = event.getSource().getPartyId();
            log.info("Start identificational level changed handling, eventId={}, partyId={}, contractorId={}", eventId, partyId, contractorId);
            Contractor contractorSource = contractorDao.get(partyId, contractorId);
            if (contractorSource == null) {
                throw new NotFoundException(String.format("Contractor not found, contractorId='%s'", contractorId));
            }
            contractorSource.setId(null);
            contractorSource.setWtime(null);
            contractorSource.setEventId(eventId);
            contractorSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            contractorSource.setIdentificationalLevel(identificationLevelChanged.name());
            contractorDao.updateNotCurrent(partyId, contractorId);
            contractorDao.save(contractorSource);
            log.info("Contract identificational level has been saved, eventId={}, contractorId={}", eventId, contractorId);
        });
    }
}
