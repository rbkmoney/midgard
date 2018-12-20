package com.rbkmoney.midgard.base.load.pollers.dominant;

import com.rbkmoney.damsel.domain.DomainObject;
import com.rbkmoney.damsel.domain_config.Operation;
import com.rbkmoney.midgard.base.load.DAO.dominant.iface.DomainObjectDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @param <T> - damsel object class (CategoryObject, CurrencyObject etc.)
 * @param <C> - jooq object class (Category, Currency etc.)
 * @param <I> - object reference id class (Integer, String etc.)
 */
@Slf4j
public abstract class AbstractDominantHandler<T, C, I> implements DominantHandler<Operation> {

    private DomainObject domainObject;

    protected DomainObject getDomainObject() {
        return domainObject;
    }

    protected abstract DomainObjectDao<C, I> getDomainObjectDao();
    protected abstract T getObject();
    protected abstract I getObjectRefId();
    protected abstract boolean acceptDomainObject();
    public abstract C convertToDatabaseObject(T object, Long versionId, boolean current);

    @Override
    public void handle(Operation operation, Long versionId) {
        T object = getObject();
        if (operation.isSetInsert()) {
            insertDomainObject(object, versionId);
        } else if (operation.isSetUpdate()) {
            updateDomainObject(object, versionId);
        } else if (operation.isSetRemove()) {
            removeDomainObject(object, versionId);
        } else {
            throw new IllegalStateException("Unknown type of operation. Only insert/update/remove supports. Operation: " + operation);
        }
    }

    @Override
    public boolean accept(Operation operation) {
        if (operation.isSetInsert()) {
            domainObject = operation.getInsert().getObject();
        } else if (operation.isSetUpdate()) {
            domainObject = operation.getUpdate().getNewObject();
        } else if (operation.isSetRemove()) {
            domainObject = operation.getRemove().getObject();
        } else {
            throw new IllegalStateException("Unknown type of operation. Only insert/update/remove supports. Operation: " + operation);
        }
        return acceptDomainObject();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertDomainObject(T object, Long versionId) {
        log.info("Start to insert '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, true));
        log.info("End to insert '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateDomainObject(T object, Long versionId) {
        log.info("Start to update '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
        getDomainObjectDao().updateNotCurrent(getObjectRefId());
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, true));
        log.info("End to update '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeDomainObject(T object, Long versionId) {
        log.info("Start to remove '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
        getDomainObjectDao().updateNotCurrent(getObjectRefId());
        getDomainObjectDao().save(convertToDatabaseObject(object, versionId, false));
        log.info("End to remove '{}' with id={}, versionId={}", object.getClass().getSimpleName(), getObjectRefId(), versionId);
    }
}
