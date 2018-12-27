package com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.shop;

import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.ShopContractChanged;
import com.rbkmoney.damsel.payment_processing.ShopEffectUnit;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.midgard.base.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.base.load.dao.party.iface.ShopDao;
import com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Shop;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShopContractChangedHandler extends AbstractClaimChangedHandler {

    private final ShopDao shopDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetShopEffect() && e.getShopEffect().getEffect().isSetContractChanged()).forEach(e -> {
            ShopEffectUnit shopEffect = e.getShopEffect();
            ShopContractChanged contractChanged = shopEffect.getEffect().getContractChanged();
            String shopId = shopEffect.getShopId();
            String partyId = event.getSource().getPartyId();
            log.info("Start shop contractChanged handling, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
            Shop shopSource = shopDao.get(partyId, shopId);
            if (shopSource == null) {
                throw new NotFoundException(String.format("Shop not found, shopId='%s'", shopId));
            }
            shopSource.setId(null);
            shopSource.setWtime(null);
            shopSource.setEventId(eventId);
            shopSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            shopSource.setContractId(contractChanged.getContractId());
            shopSource.setPayoutToolId(contractChanged.getPayoutToolId());
            shopDao.updateNotCurrent(partyId, shopId);
            shopDao.save(shopSource);
            log.info("Shop contractChanged has been saved, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
        });
    }
}
