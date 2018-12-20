package com.rbkmoney.midgard.base.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.CurrencyObject;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.load.DAO.dominant.impl.CurrencyDaoImpl;
import com.rbkmoney.midgard.base.load.pollers.dominant.AbstractDominantHandler;
import org.jooq.generated.feed.tables.pojos.Currency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyHandler extends AbstractDominantHandler<CurrencyObject, Currency, String> {

    private final CurrencyDaoImpl currencyDao;

    public CurrencyHandler(CurrencyDaoImpl currencyDao) {
        this.currencyDao = currencyDao;
    }

    @Override
    protected DomainObjectDao<Currency, String> getDomainObjectDao() {
        return currencyDao;
    }

    @Override
    protected CurrencyObject getObject() {
        return getDomainObject().getCurrency();
    }

    @Override
    protected String getObjectRefId() {
        return getObject().getRef().getSymbolicCode();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCurrency();
    }

    @Override
    public Currency convertToDatabaseObject(CurrencyObject currencyObject, Long versionId, boolean current) {
        Currency currency = new Currency();
        currency.setVersionId(versionId);
        currency.setCurrencyRefId(getObjectRefId());
        com.rbkmoney.damsel.domain.Currency data = currencyObject.getData();
        currency.setName(data.getName());
        currency.setSymbolicCode(data.getSymbolicCode());
        currency.setNumericCode(data.getNumericCode());
        currency.setExponent(data.getExponent());
        currency.setCurrent(current);
        return currency;
    }
}
