package com.rbkmoney.midgard.base.load.pollers.event_sink.party_mngmnt;

import com.rbkmoney.damsel.payment_processing.ClaimStatus;
import com.rbkmoney.damsel.payment_processing.PartyChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;

public abstract class AbstractClaimChangedHandler extends AbstractPartyManagementHandler {

    private final Filter claimCreatedFilter = new PathConditionFilter(
            new PathConditionRule("claim_created.status.accepted", new IsNullCondition().not()));

    private final Filter claimStatusChangedFilter = new PathConditionFilter(
            new PathConditionRule("claim_status_changed.status.accepted", new IsNullCondition().not()));

    @Override
    public boolean accept(PartyChange change) {
        return claimCreatedFilter.match(change) || claimStatusChangedFilter.match(change);
    }

    @Override
    public Filter<PartyChange> getFilter() {
        return claimCreatedFilter;
    }

    protected ClaimStatus getClaimStatus(PartyChange change) {
        ClaimStatus claimStatus = null;
        if (change.isSetClaimCreated()) {
            claimStatus = change.getClaimCreated().getStatus();
        } else if (change.isSetClaimStatusChanged()) {
            claimStatus = change.getClaimStatusChanged().getStatus();
        }
        return claimStatus;
    }
}
