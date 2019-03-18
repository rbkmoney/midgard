package com.rbkmoney.midgard.service.clearing.importers;

import com.rbkmoney.midgard.service.clearing.exception.DaoException;

import java.util.List;

public interface Importer {

    void getData(List<Integer> providerIds) throws DaoException;

}
