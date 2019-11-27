package com.rbkmoney.midgard.handler;

public interface Handler<T> {

    void handle(T data) throws Exception;

}
