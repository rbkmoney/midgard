package com.rbkmoney.midgard.base.load.pollers.dominant.impl;

import com.rbkmoney.damsel.domain.TermSetHierarchyObject;
import com.rbkmoney.midgard.base.load.dao.dominant.iface.DomainObjectDao;
import com.rbkmoney.midgard.base.load.dao.dominant.impl.TermSetHierarchyDaoImpl;
import com.rbkmoney.midgard.base.load.pollers.dominant.AbstractDominantHandler;
import com.rbkmoney.midgard.base.load.utils.JsonUtil;
import org.jooq.generated.feed.tables.pojos.TermSetHierarchy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TermSetHierarchyHandler extends AbstractDominantHandler<TermSetHierarchyObject, TermSetHierarchy, Integer> {

    private final TermSetHierarchyDaoImpl termSetHierarchyDao;

    public TermSetHierarchyHandler(TermSetHierarchyDaoImpl termSetHierarchyDao) {
        this.termSetHierarchyDao = termSetHierarchyDao;
    }

    @Override
    protected DomainObjectDao<TermSetHierarchy, Integer> getDomainObjectDao() {
        return termSetHierarchyDao;
    }

    @Override
    protected TermSetHierarchyObject getObject() {
        return getDomainObject().getTermSetHierarchy();
    }

    @Override
    protected Integer getObjectRefId() {
        return getObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetTermSetHierarchy();
    }

    @Override
    public TermSetHierarchy convertToDatabaseObject(TermSetHierarchyObject termSetHierarchyObject,
                                                    Long versionId,
                                                    boolean current) {
        TermSetHierarchy termSetHierarchy = new TermSetHierarchy();
        termSetHierarchy.setVersionId(versionId);
        termSetHierarchy.setTermSetHierarchyRefId(getObjectRefId());
        com.rbkmoney.damsel.domain.TermSetHierarchy data = termSetHierarchyObject.getData();
        termSetHierarchy.setName(data.getName());
        termSetHierarchy.setDescription(data.getDescription());
        if (data.isSetParentTerms()) {
            termSetHierarchy.setParentTermsRefId(data.getParentTerms().getId());
        }
        termSetHierarchy.setTermSetsJson(JsonUtil.objectToJsonString(data.getTermSets().stream()
                .map(JsonUtil::tBaseToJsonNode)
                .collect(Collectors.toList())));
        termSetHierarchy.setCurrent(current);
        return termSetHierarchy;
    }
}
