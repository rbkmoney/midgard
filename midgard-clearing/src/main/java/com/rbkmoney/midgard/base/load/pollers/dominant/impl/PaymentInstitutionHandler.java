package com.rbkmoney.midgard.base.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.PaymentInstitutionObject;
import com.rbkmoney.midgard.base.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.load.dao.dominant.impl.PaymentInstitutionDaoImpl;
import com.rbkmoney.midgard.base.load.pollers.dominant.AbstractDominantHandler;
import com.rbkmoney.midgard.base.load.utils.JsonUtil;
import org.jooq.generated.feed.tables.pojos.PaymentInstitution;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PaymentInstitutionHandler extends AbstractDominantHandler<PaymentInstitutionObject, PaymentInstitution, Integer> {

    private final PaymentInstitutionDaoImpl paymentInstitutionDao;

    public PaymentInstitutionHandler(PaymentInstitutionDaoImpl paymentInstitutionDao) {
        this.paymentInstitutionDao = paymentInstitutionDao;
    }

    @Override
    protected DomainObjectDao<PaymentInstitution, Integer> getDomainObjectDao() {
        return paymentInstitutionDao;
    }

    @Override
    protected PaymentInstitutionObject getObject() {
        return getDomainObject().getPaymentInstitution();
    }

    @Override
    protected Integer getObjectRefId() {
        return getObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetPaymentInstitution();
    }

    @Override
    public PaymentInstitution convertToDatabaseObject(PaymentInstitutionObject paymentInstitutionObject, Long versionId, boolean current) {
        PaymentInstitution paymentInstitution = new PaymentInstitution();
        paymentInstitution.setVersionId(versionId);
        paymentInstitution.setPaymentInstitutionRefId(getObjectRefId());
        com.rbkmoney.damsel.domain.PaymentInstitution data = paymentInstitutionObject.getData();
        paymentInstitution.setName(data.getName());
        paymentInstitution.setDescription(data.getDescription());
        if (data.isSetCalendar()) {
            paymentInstitution.setCalendarRefId(data.getCalendar().getId());
        }
        paymentInstitution.setSystemAccountSetJson(JsonUtil.tBaseToJsonString(data.getSystemAccountSet()));
        paymentInstitution.setDefaultContractTemplateJson(JsonUtil.tBaseToJsonString(data.getDefaultContractTemplate()));
        if (data.isSetDefaultWalletContractTemplate()) {
            paymentInstitution.setDefaultWalletContractTemplateJson(JsonUtil.tBaseToJsonString(data.getDefaultWalletContractTemplate()));
        }
        paymentInstitution.setProvidersJson(JsonUtil.tBaseToJsonString(data.getProviders()));
        paymentInstitution.setInspectorJson(JsonUtil.tBaseToJsonString(data.getInspector()));
        paymentInstitution.setRealm(data.getRealm().name());
        paymentInstitution.setResidencesJson(JsonUtil.objectToJsonString(data.getResidences().stream().map(Enum::name).collect(Collectors.toSet())));
        paymentInstitution.setCurrent(current);
        return paymentInstitution;
    }
}
