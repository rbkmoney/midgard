package com.rbkmoney.midgard.base.load.dao.dominant.iface;


import com.rbkmoney.midgard.base.clearing.exception.DaoException;
import com.rbkmoney.midgard.base.clearing.helpers.dao.common.Dao;

public interface DominantDao extends Dao {
    Long getLastVersionId() throws DaoException;
}
