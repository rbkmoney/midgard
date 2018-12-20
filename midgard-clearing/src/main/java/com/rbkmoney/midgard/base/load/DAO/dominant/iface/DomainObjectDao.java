package com.rbkmoney.midgard.base.load.DAO.dominant.iface;

import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.DAO.common.Dao;

public interface DomainObjectDao<T, I> extends Dao {


    Long save(T domainObject) throws DaoException;

    void updateNotCurrent(I objectId) throws DaoException;
}
