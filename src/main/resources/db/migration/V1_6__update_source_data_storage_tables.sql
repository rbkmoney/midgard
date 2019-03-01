truncate feed.payout cascade;

ALTER TABLE feed.payout ADD COLUMN amount BIGINT;
ALTER TABLE feed.payout ADD COLUMN fee    BIGINT;
ALTER TABLE feed.payout ADD COLUMN currency_code CHARACTER VARYING;
ALTER TABLE feed.payout ADD COLUMN wallet_id     CHARACTER VARYING;

ALTER TABLE feed.payout DROP COLUMN initiator_id;
ALTER TABLE feed.payout DROP COLUMN initiator_type;

ALTER TABLE feed.contract   ALTER COLUMN revision DROP NOT NULL;
ALTER TABLE feed.contractor ALTER COLUMN revision DROP NOT NULL;
ALTER TABLE feed.shop       ALTER COLUMN revision DROP NOT NULL;

ALTER TABLE feed.payout_tool ADD COLUMN payout_tool_info_wallet_info_wallet_id CHARACTER VARYING;
