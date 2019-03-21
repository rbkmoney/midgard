package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.shop;

import com.rbkmoney.damsel.domain.Blocking;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.load.dao.party.iface.ShopDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Shop;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ShopBlockingHandler extends AbstractPartyManagementHandler {

    private final ShopDao shopDao;

    private final Filter filter;

    public ShopBlockingHandler(ShopDao shopDao) {
        this.shopDao = shopDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "shop_blocking",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        Blocking blocking = change.getShopBlocking().getBlocking();
        String shopId = change.getShopBlocking().getShopId();
        String partyId = event.getSource().getPartyId();
        log.info("Start shop blocking handling, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
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
        shopSource.setBlocking(TBaseUtil.unionFieldToEnum(blocking, org.jooq.generated.feed.enums.Blocking.class));
        if (blocking.isSetUnblocked()) {
            shopSource.setBlockingUnblockedReason(blocking.getUnblocked().getReason());
            shopSource.setBlockingUnblockedSince(TypeUtil.stringToLocalDateTime(blocking.getUnblocked().getSince()));
            shopSource.setBlockingBlockedReason(null);
            shopSource.setBlockingBlockedSince(null);
        } else if (blocking.isSetBlocked()) {
            shopSource.setBlockingUnblockedReason(null);
            shopSource.setBlockingUnblockedSince(null);
            shopSource.setBlockingBlockedReason(blocking.getBlocked().getReason());
            shopSource.setBlockingBlockedSince(TypeUtil.stringToLocalDateTime(blocking.getBlocked().getSince()));
        }
        shopDao.updateNotCurrent(partyId, shopId);
        shopDao.save(shopSource);
        log.info("Shop blocking has been saved, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
