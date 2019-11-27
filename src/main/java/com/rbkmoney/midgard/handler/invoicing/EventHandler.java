package com.rbkmoney.midgard.handler.invoicing;

import com.rbkmoney.geck.filter.Filter;

public interface EventHandler<T, E> {

    boolean accept(T change);

    void handle(T change, E event, Integer changeId) throws Exception;

    Filter<T> getFilter();

}
