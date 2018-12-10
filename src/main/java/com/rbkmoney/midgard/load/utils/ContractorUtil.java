package com.rbkmoney.midgard.load.utils;

import com.rbkmoney.damsel.domain.InternationalLegalEntity;
import com.rbkmoney.damsel.domain.RussianLegalEntity;
import com.rbkmoney.damsel.domain.RussianPrivateEntity;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import org.jooq.generated.feed.enums.ContractorType;
import org.jooq.generated.feed.enums.LegalEntity;
import org.jooq.generated.feed.enums.PrivateEntity;
import org.jooq.generated.feed.tables.pojos.Contractor;

public class ContractorUtil {

    public static Contractor convertContractor(long eventId,
                                               String eventCreatedAt,
                                               String partyId,
                                               long partyRevision,
                                               com.rbkmoney.damsel.domain.Contractor contractorSource,
                                               String contractorId) {
        Contractor contractor = new Contractor();
        contractor.setEventId(eventId);
        contractor.setEventCreatedAt(TypeUtil.stringToLocalDateTime(eventCreatedAt));
        contractor.setRevision(partyRevision);
        contractor.setPartyId(partyId);
        contractor.setContractorId(contractorId);
        contractor.setType(TBaseUtil.unionFieldToEnum(contractorSource, ContractorType.class));
        if (contractorSource.isSetRegisteredUser()) {
            contractor.setRegisteredUserEmail(contractorSource.getRegisteredUser().getEmail());
        } else if (contractorSource.isSetLegalEntity()) {
            contractor.setLegalEntity(TBaseUtil.unionFieldToEnum(contractorSource.getLegalEntity(), LegalEntity.class));
            if (contractorSource.getLegalEntity().isSetRussianLegalEntity()) {
                RussianLegalEntity russianLegalEntity = contractorSource.getLegalEntity().getRussianLegalEntity();
                contractor.setRussianLegalEntityRegisteredName(russianLegalEntity.getRegisteredName());
                contractor.setRussianLegalEntityRegisteredNumber(russianLegalEntity.getRegisteredNumber());
                contractor.setRussianLegalEntityInn(russianLegalEntity.getInn());
                contractor.setRussianLegalEntityActualAddress(russianLegalEntity.getActualAddress());
                contractor.setRussianLegalEntityPostAddress(russianLegalEntity.getPostAddress());
                contractor.setRussianLegalEntityRepresentativePosition(russianLegalEntity.getRepresentativePosition());
                contractor.setRussianLegalEntityRepresentativeFullName(russianLegalEntity.getRepresentativeFullName());
                contractor.setRussianLegalEntityRepresentativeDocument(russianLegalEntity.getRepresentativeDocument());
                contractor.setRussianLegalEntityRussianBankAccount(russianLegalEntity.getRussianBankAccount().getAccount());
                contractor.setRussianLegalEntityRussianBankName(russianLegalEntity.getRussianBankAccount().getBankName());
                contractor.setRussianLegalEntityRussianBankPostAccount(russianLegalEntity.getRussianBankAccount().getBankPostAccount());
                contractor.setRussianLegalEntityRussianBankBik(russianLegalEntity.getRussianBankAccount().getBankBik());
            } else if (contractorSource.getLegalEntity().isSetInternationalLegalEntity()) {
                InternationalLegalEntity internationalLegalEntity = contractorSource.getLegalEntity().getInternationalLegalEntity();
                contractor.setInternationalLegalEntityLegalName(internationalLegalEntity.getLegalName());
                contractor.setInternationalLegalEntityTradingName(internationalLegalEntity.getTradingName());
                contractor.setInternationalLegalEntityRegisteredAddress(internationalLegalEntity.getRegisteredAddress());
                contractor.setInternationalLegalEntityActualAddress(internationalLegalEntity.getActualAddress());
                contractor.setInternationalLegalEntityRegisteredNumber(internationalLegalEntity.getRegisteredNumber());
            }
        } else if (contractorSource.isSetPrivateEntity()) {
            contractor.setPrivateEntity(TBaseUtil.unionFieldToEnum(contractorSource.getPrivateEntity(), PrivateEntity.class));
            if (contractorSource.getPrivateEntity().isSetRussianPrivateEntity()) {
                RussianPrivateEntity russianPrivateEntity = contractorSource.getPrivateEntity().getRussianPrivateEntity();
                contractor.setRussianPrivateEntityFirstName(russianPrivateEntity.getFirstName());
                contractor.setRussianPrivateEntitySecondName(russianPrivateEntity.getSecondName());
                contractor.setRussianPrivateEntityMiddleName(russianPrivateEntity.getMiddleName());
                contractor.setRussianPrivateEntityPhoneNumber(russianPrivateEntity.getContactInfo().getPhoneNumber());
                contractor.setRussianPrivateEntityEmail(russianPrivateEntity.getContactInfo().getEmail());
            }
        }
        return contractor;
    }
}
