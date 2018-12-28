package com.rbkmoney.midgard.service.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.PayoutMethodDefinition;
import com.rbkmoney.damsel.domain.PayoutMethodObject;
import com.rbkmoney.midgard.service.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.service.load.dao.dominant.impl.PayoutMethodDaoImpl;
import com.rbkmoney.midgard.service.load.pollers.dominant.AbstractDominantHandler;
import org.jooq.generated.feed.tables.pojos.PayoutMethod;
import org.springframework.stereotype.Component;

@Component
public class PayoutMethodHandler extends AbstractDominantHandler<PayoutMethodObject, PayoutMethod, String> {

    private final PayoutMethodDaoImpl payoutMethodDao;

    public PayoutMethodHandler(PayoutMethodDaoImpl payoutMethodDao) {
        this.payoutMethodDao = payoutMethodDao;
    }

    @Override
    protected DomainObjectDao<PayoutMethod, String> getDomainObjectDao() {
        return payoutMethodDao;
    }

    @Override
    protected PayoutMethodObject getObject() {
        return getDomainObject().getPayoutMethod();
    }

    @Override
    protected String getObjectRefId() {
        return getObject().getRef().getId().name();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetPayoutMethod();
    }

    @Override
    public PayoutMethod convertToDatabaseObject(PayoutMethodObject payoutMethodObject, Long versionId, boolean current) {
        PayoutMethod payoutMethod = new PayoutMethod();
        payoutMethod.setVersionId(versionId);
        payoutMethod.setPayoutMethodRefId(getObjectRefId());
        PayoutMethodDefinition data = payoutMethodObject.getData();
        payoutMethod.setName(data.getName());
        payoutMethod.setDescription(data.getDescription());
        payoutMethod.setCurrent(current);
        return payoutMethod;
    }
}
