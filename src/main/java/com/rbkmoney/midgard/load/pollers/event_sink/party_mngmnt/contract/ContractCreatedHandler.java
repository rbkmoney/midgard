package com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.contract;

import com.rbkmoney.damsel.payment_processing.ContractEffectUnit;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.midgard.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.load.DAO.party.iface.*;
import com.rbkmoney.midgard.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import com.rbkmoney.midgard.load.utils.ContractUtil;
import com.rbkmoney.midgard.load.utils.ContractorUtil;
import org.jooq.generated.feed.enums.ContractStatus;
import org.jooq.generated.feed.tables.pojos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class ContractCreatedHandler extends AbstractClaimChangedHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ContractDao contractDao;

    private final ContractorDao contractorDao;

    private final PartyDao partyDao;

    private final ContractAdjustmentDao contractAdjustmentDao;

    private final PayoutToolDao payoutToolDao;

    public ContractCreatedHandler(ContractDao contractDao,
                                  ContractorDao contractorDao,
                                  PartyDao partyDao,
                                  ContractAdjustmentDao contractAdjustmentDao,
                                  PayoutToolDao payoutToolDao) {
        this.contractDao = contractDao;
        this.contractorDao = contractorDao;
        this.partyDao = partyDao;
        this.contractAdjustmentDao = contractAdjustmentDao;
        this.payoutToolDao = payoutToolDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetContractEffect() && e.getContractEffect().getEffect().isSetCreated()).forEach(e -> {
            ContractEffectUnit contractEffectUnit = e.getContractEffect();
            com.rbkmoney.damsel.domain.Contract contractCreated = contractEffectUnit.getEffect().getCreated();
            String contractId = contractEffectUnit.getContractId();
            String partyId = event.getSource().getPartyId();
            log.info("Start contract created handling, eventId={}, partyId={}, contractId={}", eventId, partyId, contractId);
            Contract contract = new Contract();
            contract.setEventId(eventId);
            contract.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            Party partySource = partyDao.get(partyId);
            if (partySource == null) {
                throw new NotFoundException(String.format("Party not found, partyId='%s'", partyId));
            }
            contract.setRevision(partySource.getRevision());
            contract.setContractId(contractId);
            contract.setPartyId(partyId);
            if (contractCreated.isSetPaymentInstitution()) {
                contract.setPaymentInstitutionId(contractCreated.getPaymentInstitution().getId());
            }
            contract.setCreatedAt(TypeUtil.stringToLocalDateTime(contractCreated.getCreatedAt()));
            if (contractCreated.isSetValidSince()) {
                contract.setValidSince(TypeUtil.stringToLocalDateTime(contractCreated.getValidSince()));
            }
            if (contractCreated.isSetValidUntil()) {
                contract.setValidUntil(TypeUtil.stringToLocalDateTime(contractCreated.getValidUntil()));
            }
            contract.setStatus(TBaseUtil.unionFieldToEnum(contractCreated.getStatus(), ContractStatus.class));
            if (contractCreated.getStatus().isSetTerminated()) {
                contract.setStatusTerminatedAt(TypeUtil.stringToLocalDateTime(contractCreated.getStatus()
                        .getTerminated().getTerminatedAt()));
            }
            contract.setTermsId(contractCreated.getTerms().getId());
            if (contractCreated.isSetLegalAgreement()) {
                ContractUtil.fillContractLegalAgreementFields(contract, contractCreated.getLegalAgreement());
            }
            if (contractCreated.isSetReportPreferences()
                    && contractCreated.getReportPreferences().isSetServiceAcceptanceActPreferences()) {
                ContractUtil.fillReportPreferences(contract,
                        contractCreated.getReportPreferences().getServiceAcceptanceActPreferences());
            }

            String contractorId = "";
            if (contractCreated.isSetContractorId()) {
                contractorId = contractCreated.getContractorId();
            } else if (contractCreated.isSetContractor()) {
                contractorId = UUID.randomUUID().toString();
            }
            
            contract.setContractorId(contractorId);
            long cntrctId = contractDao.save(contract);

            if (contractCreated.isSetContractor()) {
                Contractor contractor = ContractorUtil.convertContractor(eventId, event.getCreatedAt(),
                        partyId, partySource.getRevision(), contractCreated.getContractor(), contractorId);
                contractorDao.save(contractor);
            }

            List<ContractAdjustment> adjustments = ContractUtil.convertContractAdjustments(contractCreated, cntrctId);
            contractAdjustmentDao.save(adjustments);

            List<PayoutTool> payoutTools = ContractUtil.convertPayoutTools(contractCreated, cntrctId);
            payoutToolDao.save(payoutTools);

            log.info("Contract has been saved, eventId={}, partyId={}, contractId={}", eventId, partyId, contractId);
        });
    }
}
