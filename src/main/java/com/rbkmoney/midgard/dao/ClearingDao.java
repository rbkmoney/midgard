package com.rbkmoney.midgard.dao;

public interface ClearingDao<T, V> extends Dao {

    Long save(T element);

    T get(V id);

}
