package com.rbkmoney.midgard.adapter.mts.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TerminalOptionalJson {

    private String smmcc;
    private String merchantName;
    private String merchantId;
    private String terminalId;
    private String smAddress;
    private String smPostCode;
    private String smCity;

    @JsonCreator
    public TerminalOptionalJson(@JsonProperty("SMMCC") String smMcc,
                                @JsonProperty("merchant_name") String merchantName,
                                @JsonProperty("merchant_id") String merchantId,
                                @JsonProperty("term_id") String terminalId,
                                @JsonProperty("SMADDRESS") String smAddress,
                                @JsonProperty("SMPOSTCODE") String smPostCode,
                                @JsonProperty("SmCity") String smCity) {
        this.smmcc = smMcc;
        this.merchantName = merchantName;
        this.merchantId = merchantId;
        this.terminalId = terminalId;
        this.smAddress = smAddress;
        this.smPostCode = smPostCode;
        this.smCity = smCity;
    }

}
