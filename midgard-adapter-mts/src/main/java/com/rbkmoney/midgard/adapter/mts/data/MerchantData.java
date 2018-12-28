package com.rbkmoney.midgard.adapter.mts.data;

import lombok.Data;

@Data
public class MerchantData {

    private String        merchantId;
    private String        merchantName;
    private String        merchantAddress;
    private String        merchantCountry;
    private String        merchantCity;
    private String        merchantPostalCode;

}
