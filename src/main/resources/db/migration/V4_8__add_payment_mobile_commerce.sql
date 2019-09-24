CREATE TYPE feed.mobile_operator_type AS ENUM ('mts', 'beeline', 'megafone', 'tele2', 'yota');

ALTER TABLE feed.payment ADD COLUMN payer_mobile_operator  feed.mobile_operator_type;
ALTER TABLE feed.payment ADD COLUMN payer_mobile_phone_cc  CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN payer_mobile_phone_ctn CHARACTER VARYING;