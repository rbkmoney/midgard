package com.rbkmoney.midgard.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.InspectorObject;
import com.rbkmoney.midgard.load.DAO.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.load.DAO.dominant.impl.InspectorDaoImpl;
import com.rbkmoney.midgard.load.pollers.dominant.AbstractDominantHandler;
import com.rbkmoney.midgard.load.utils.JsonUtil;
import org.jooq.generated.feed.tables.pojos.Inspector;
import org.springframework.stereotype.Component;

@Component
public class InspectorHandler extends AbstractDominantHandler<InspectorObject, Inspector, Integer> {

    private final InspectorDaoImpl inspectorDao;

    public InspectorHandler(InspectorDaoImpl inspectorDao) {
        this.inspectorDao = inspectorDao;
    }

    @Override
    protected DomainObjectDao<Inspector, Integer> getDomainObjectDao() {
        return inspectorDao;
    }

    @Override
    protected InspectorObject getObject() {
        return getDomainObject().getInspector();
    }

    @Override
    protected Integer getObjectRefId() {
        return getObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetInspector();
    }

    @Override
    public Inspector convertToDatabaseObject(InspectorObject inspectorObject, Long versionId, boolean current) {
        Inspector inspector = new Inspector();
        inspector.setVersionId(versionId);
        inspector.setInspectorRefId(getObjectRefId());
        com.rbkmoney.damsel.domain.Inspector data = inspectorObject.getData();
        inspector.setName(data.getName());
        inspector.setDescription(data.getDescription());
        inspector.setProxyRefId(data.getProxy().getRef().getId());
        inspector.setProxyAdditionalJson(JsonUtil.objectToJsonString(data.getProxy().getAdditional()));
        if (data.isSetFallbackRiskScore()) {
            inspector.setFallbackRiskScore(data.getFallbackRiskScore().name());
        }
        inspector.setCurrent(current);
        return inspector;
    }
}
