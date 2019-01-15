package com.rbkmoney.midgard.service.clearing.dao.common;

public interface ClearingDao<T> extends Dao {

    Long save(T element);

     T get(String id);

}
