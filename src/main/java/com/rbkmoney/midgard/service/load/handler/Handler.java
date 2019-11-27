package com.rbkmoney.midgard.service.load.handler;

import com.rbkmoney.geck.filter.Filter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.thrift.TException;

public interface Handler<T, E> {

    boolean accept(T change);

    void handle(T change, E event, Integer changeId) throws Exception;

    Filter<T> getFilter();

}
