package com.rbkmoney.midgard.service.load.converter;

public interface BinaryConverter<T> {

    T convert(byte[] bin, Class<T> clazz);

}
