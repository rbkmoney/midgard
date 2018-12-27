package com.rbkmoney.midgard.base.clearing.helpers.dao.common;

public interface ClearingDao<T> extends Dao {

    Long save(T element);

     T get(String id);

}
