package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.shop;

import com.rbkmoney.damsel.domain.ShopDetails;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.damsel.payment_processing.ShopEffectUnit;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.party.iface.ShopDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractClaimChangedHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Shop;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class ShopDetailsChangedHandler extends AbstractClaimChangedHandler {

    private final ShopDao shopDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        getClaimStatus(change).getAccepted().getEffects().stream()
                .filter(e -> e.isSetShopEffect() && e.getShopEffect().getEffect().isSetDetailsChanged()).forEach(e -> {
            ShopEffectUnit shopEffect = e.getShopEffect();
            ShopDetails detailsChanged = shopEffect.getEffect().getDetailsChanged();
            String shopId = shopEffect.getShopId();
            String partyId = event.getSource().getPartyId();
            log.info("Start shop detailsChanged handling, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
            Shop shopSource = shopDao.get(partyId, shopId);
            if (shopSource == null) {
                // TODO: исправить после того как прольется БД
                log.error("Shop not found, shopId='{}'", shopId);
                return;
                //throw new NotFoundException(String.format("Shop not found, shopId='%s'", shopId));
            }
            shopSource.setId(null);
            shopSource.setRevision(null);
            shopSource.setWtime(null);
            shopSource.setEventId(eventId);
            shopSource.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
            shopSource.setDetailsName(detailsChanged.getName());
            shopSource.setDetailsDescription(detailsChanged.getDescription());
            shopDao.updateNotCurrent(partyId, shopId);
            shopDao.save(shopSource);
            log.info("Shop detailsChanged has been saved, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
        });
    }
}
