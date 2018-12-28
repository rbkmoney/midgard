package com.rbkmoney.midgard.service.load.dao.dominant.iface;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.Dao;

public interface DomainObjectDao<T, I> extends Dao {

    Long save(T domainObject) throws DaoException;

    void updateNotCurrent(I objectId) throws DaoException;
}
