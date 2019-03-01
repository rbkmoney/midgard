package com.rbkmoney.midgard.service.clearing.dao.common;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;

public interface ClearingDao<T, V> extends Dao {

    Long save(T element) throws DaoException;

     T get(V id) throws DaoException;

}
