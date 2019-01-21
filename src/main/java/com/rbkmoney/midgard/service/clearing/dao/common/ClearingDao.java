package com.rbkmoney.midgard.service.clearing.dao.common;

public interface ClearingDao<T, V> extends Dao {

    Long save(T element);

     T get(V id);

}
