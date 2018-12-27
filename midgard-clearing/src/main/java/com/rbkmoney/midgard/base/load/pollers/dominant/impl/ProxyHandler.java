package com.rbkmoney.midgard.base.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.ProxyDefinition;
import com.rbkmoney.damsel.domain.ProxyObject;
import com.rbkmoney.midgard.base.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.load.dao.dominant.impl.ProxyDaoImpl;
import com.rbkmoney.midgard.base.load.pollers.dominant.AbstractDominantHandler;
import com.rbkmoney.midgard.base.load.utils.JsonUtil;
import org.jooq.generated.feed.tables.pojos.Proxy;
import org.springframework.stereotype.Component;

@Component
public class ProxyHandler extends AbstractDominantHandler<ProxyObject, Proxy, Integer> {

    private final ProxyDaoImpl proxyDao;

    public ProxyHandler(ProxyDaoImpl proxyDao) {
        this.proxyDao = proxyDao;
    }

    @Override
    protected DomainObjectDao<Proxy, Integer> getDomainObjectDao() {
        return proxyDao;
    }

    @Override
    protected ProxyObject getObject() {
        return getDomainObject().getProxy();
    }

    @Override
    protected Integer getObjectRefId() {
        return getObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetProxy();
    }

    @Override
    public Proxy convertToDatabaseObject(ProxyObject proxyObject, Long versionId, boolean current) {
        Proxy proxy = new Proxy();
        proxy.setVersionId(versionId);
        proxy.setProxyRefId(getObjectRefId());
        ProxyDefinition data = proxyObject.getData();
        proxy.setName(data.getName());
        proxy.setDescription(data.getDescription());
        proxy.setUrl(data.getUrl());
        proxy.setOptionsJson(JsonUtil.objectToJsonString(data.getOptions()));
        proxy.setCurrent(current);
        return proxy;
    }
}
