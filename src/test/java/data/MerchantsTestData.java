package data;

import org.jooq.generated.midgard.tables.pojos.Merchant;

import java.util.ArrayList;
import java.util.List;

/** Класс с тестовыми данными мерчантов для тестов */
public final class MerchantsTestData {

    public static List<Merchant> getMerchants() {
        List<Merchant> merchants = new ArrayList<>();
        merchants.add(getMerchant());
        return merchants;
    }

    public static Merchant getMerchant() {
        Merchant merchant = new Merchant();
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
