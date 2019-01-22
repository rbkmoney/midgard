package com.rbkmoney.midgard.service.clearing.handlers;

public interface Handler<T> {

    void handle(T data) throws Exception;

}
