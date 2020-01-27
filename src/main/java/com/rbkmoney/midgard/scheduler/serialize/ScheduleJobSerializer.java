package com.rbkmoney.midgard.scheduler.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.midgard.scheduler.model.AdapterJobContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ScheduleJobSerializer {

    private final ObjectMapper mapper;

    public byte[] writeByte(AdapterJobContext obj) {
        try {
            return mapper.writeValueAsBytes(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public AdapterJobContext read(byte[] data) {
        try {
            return mapper.readValue(data, AdapterJobContext.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
