package com.rbkmoney.midgard.service.load.dao.dominant.iface;


import com.rbkmoney.midgard.service.clearing.exception.DaoException;
import com.rbkmoney.midgard.service.clearing.helpers.DAO.common.Dao;

public interface DominantDao extends Dao {
    Long getLastVersionId() throws DaoException;
}
