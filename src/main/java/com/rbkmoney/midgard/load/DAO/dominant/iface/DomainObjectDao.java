package com.rbkmoney.midgard.load.DAO.dominant.iface;

import com.rbkmoney.midgard.clearing.helpers.DAO.common.Dao;
import com.rbkmoney.midgard.clearing.exception.DaoException;

public interface DomainObjectDao<T, I> extends Dao {


    Long save(T domainObject) throws DaoException;

    void updateNotCurrent(I objectId) throws DaoException;
}
