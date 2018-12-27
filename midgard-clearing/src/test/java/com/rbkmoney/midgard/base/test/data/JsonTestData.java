package com.rbkmoney.midgard.base.test.data;

import com.rbkmoney.midgard.base.clearing.data.TerminalOptionalJson;

public final class JsonTestData {

    public static final String OPTIONAL_JSON = "{\"SMMCC\":\"4722\",\"merchant_name\":\"milatur.rf@RBKmoney\"," +
                "\"merchant_id\":\"24275830\",\"term_id\":\"24275830\"," +
                "\"PFSNAME\":\"milatur.rf\",\"SMID\":\"3df971e8ac7b\"," +
                "\"SMADDRESS\":\"Uric 117\",\"SMPOSTCODE\":\"660049\"" +
                ",\"SmCity\":\"Krasnoyar\"}";

    public static TerminalOptionalJson getTerminalOptionalJson() {
        TerminalOptionalJson json = new TerminalOptionalJson();
        json.setSmmcc("4722");
        json.setMerchantName("milatur.rf@RBKmoney");
        json.setMerchantId("24275830");
        json.setTerminalId("24275830");
        json.setSmAddress("Uric 117");
        json.setSmPostCode("660049");
        json.setSmCity("Krasnoyar");
        return json;
    }


    private JsonTestData() {}

}
