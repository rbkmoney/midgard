package com.rbkmoney.midgard.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import org.apache.thrift.TBase;

import java.io.IOException;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String tBaseToJsonString(TBase tBase) {
        try {
            return new TBaseProcessor().process(tBase, new JsonHandler()).toString();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't convert to json string: " + tBase, e);
        }
    }

    public static JsonNode tBaseToJsonNode(TBase tBase) {
        try {
            return new TBaseProcessor().process(tBase, new JsonHandler());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't convert to json node: " + tBase, e);
        }
    }

    public static String objectToJsonString(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't convert object to json string: " + o, e);
        }
    }

    private JsonUtil() {}

}
