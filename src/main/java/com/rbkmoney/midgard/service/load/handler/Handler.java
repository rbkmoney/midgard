package com.rbkmoney.midgard.service.load.handler;

import com.rbkmoney.geck.filter.Filter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.thrift.TException;

public interface Handler<T, E> {

    default boolean accept(T change) {
        return getFilter().match(change);
    }

    default void handle(T change, E event) {
        throw new NotImplementedException("Override it!");
    }

    default void handle(T change, E event, Integer changeId) throws Exception {
        throw new NotImplementedException("Override it!");
    }

    Filter<T> getFilter();

}
