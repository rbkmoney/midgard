package com.rbkmoney.midgard.base.load.utils;

import com.rbkmoney.damsel.domain.ShopAccount;
import org.jooq.generated.feed.tables.pojos.Shop;

public class ShopUtil {

    public static void fillShopAccount(Shop shop, ShopAccount shopAccount) {
        shop.setAccountCurrencyCode(shopAccount.getCurrency().getSymbolicCode());
        shop.setAccountGuarantee(shopAccount.getGuarantee());
        shop.setAccountSettlement(shopAccount.getSettlement());
        shop.setAccountPayout(shopAccount.getPayout());
    }
}
