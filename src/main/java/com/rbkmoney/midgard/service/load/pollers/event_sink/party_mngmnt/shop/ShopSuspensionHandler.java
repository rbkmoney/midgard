package com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.shop;

import com.rbkmoney.damsel.domain.Suspension;
import com.rbkmoney.damsel.payment_processing.Event;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.common.util.TBaseUtil;
import com.rbkmoney.geck.common.util.TypeUtil;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.midgard.service.clearing.exception.NotFoundException;
import com.rbkmoney.midgard.service.load.dao.party.iface.ShopDao;
import com.rbkmoney.midgard.service.load.pollers.event_sink.party_mngmnt.AbstractPartyManagementHandler;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.feed.tables.pojos.Shop;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class ShopSuspensionHandler extends AbstractPartyManagementHandler {

    private final ShopDao shopDao;

    private final Filter filter;

    public ShopSuspensionHandler(ShopDao shopDao) {
        this.shopDao = shopDao;
        this.filter = new PathConditionFilter(new PathConditionRule(
                "shop_suspension",
                new IsNullCondition().not()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void handle(PartyChange change, Event event) {
        long eventId = event.getId();
        Suspension suspension = change.getShopSuspension().getSuspension();
        String shopId = change.getShopSuspension().getShopId();
        String partyId = event.getSource().getPartyId();
        log.info("Start shop suspension handling, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
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
        shopSource.setSuspension(TBaseUtil.unionFieldToEnum(suspension, org.jooq.generated.feed.enums.Suspension.class));
        if (suspension.isSetActive()) {
            shopSource.setSuspensionActiveSince(TypeUtil.stringToLocalDateTime(suspension.getActive().getSince()));
            shopSource.setSuspensionSuspendedSince(null);
        } else if (suspension.isSetSuspended()) {
            shopSource.setSuspensionActiveSince(null);
            shopSource.setSuspensionSuspendedSince(TypeUtil.stringToLocalDateTime(suspension.getSuspended().getSince()));
        }
        shopDao.updateNotCurrent(partyId, shopId);
        shopDao.save(shopSource);
        log.info("Shop suspension has been saved, eventId={}, partyId={}, shopId={}", eventId, partyId, shopId);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return filter;
    }
}
