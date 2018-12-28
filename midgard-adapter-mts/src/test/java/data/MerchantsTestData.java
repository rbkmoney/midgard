package data;

import com.rbkmoney.midgard.adapter.mts.data.MerchantData;

import java.util.ArrayList;
import java.util.List;

/** Класс с тестовыми данными мерчантов для тестов */
public final class MerchantsTestData {

    public static List<MerchantData> getMerchants() {
        List<MerchantData> merchants = new ArrayList<>();
        merchants.add(getMerchant());
        return merchants;
    }

    public static MerchantData getMerchant() {
        MerchantData merchant = new MerchantData();
        merchant.setMerchantId("29003001");
        merchant.setMerchantName("231285*EPS");
        merchant.setMerchantAddress("some address");
        merchant.setMerchantCity("643");
        merchant.setMerchantCountry("Moscow");
        merchant.setMerchantPostalCode("117112");
        return merchant;
    }

    private MerchantsTestData() {}

}
