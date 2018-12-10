package com.rbkmoney.midgard.load.DAO.dominant.iface;


import com.rbkmoney.midgard.clearing.helpers.DAO.common.Dao;
import com.rbkmoney.midgard.clearing.exception.DaoException;

public interface DominantDao extends Dao {
    Long getLastVersionId() throws DaoException;
}
