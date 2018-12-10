CREATE SCHEMA IF NOT EXISTS feed;

-- invoices --

CREATE TYPE feed.invoice_status AS ENUM('unpaid', 'paid', 'cancelled', 'fulfilled');

CREATE TABLE feed.invoice(
  id                       BIGSERIAL NOT NULL,
  event_id                 BIGINT NOT NULL,
  event_created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_id               CHARACTER VARYING NOT NULL,
  party_id                 CHARACTER VARYING NOT NULL,
  shop_id                  CHARACTER VARYING NOT NULL,
  party_revision           BIGINT,
  created_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  status                   feed.invoice_status NOT NULL,
  status_cancelled_details CHARACTER VARYING,
  status_fulfilled_details CHARACTER VARYING,
  details_product          CHARACTER VARYING NOT NULL,
  details_description      CHARACTER VARYING,
  due                      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  amount                   BIGINT NOT NULL,
  currency_code            CHARACTER VARYING NOT NULL,
  context                  BYTEA,
  template_id              CHARACTER VARYING,
  wtime                    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                  BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT invoice_pkey PRIMARY KEY (id)
);

CREATE INDEX invoice_event_id on feed.invoice(event_id);
CREATE INDEX invoice_event_created_at on feed.invoice(event_created_at);
CREATE INDEX invoice_invoice_id on feed.invoice(invoice_id);
CREATE INDEX invoice_party_id on feed.invoice(party_id);
CREATE INDEX invoice_status on feed.invoice(status);
CREATE INDEX invoice_created_at on feed.invoice(created_at);

CREATE TABLE feed.invoice_cart (
  id            BIGSERIAL NOT NULL,
  inv_id        BIGINT NOT NULL,
  product       CHARACTER VARYING NOT NULL,
  quantity      INT NOT NULL,
  amount        BIGINT NOT NULL,
  currency_code CHARACTER VARYING NOT NULL,
  metadata_json CHARACTER VARYING NOT NULL,
  CONSTRAINT invoice_cart_pkey PRIMARY KEY (id),
  CONSTRAINT fk_cart_to_invoice FOREIGN KEY (inv_id) REFERENCES feed.invoice(id)
);

CREATE INDEX invoice_cart_inv_id on feed.invoice_cart(inv_id);

-- payments --

CREATE TYPE feed.payment_status AS ENUM ('pending', 'processed', 'captured', 'cancelled', 'refunded', 'failed');
CREATE TYPE feed.payer_type AS ENUM('payment_resource', 'customer', 'recurrent');
CREATE TYPE feed.payment_tool_type AS ENUM('bank_card', 'payment_terminal', 'digital_wallet');
CREATE TYPE feed.payment_flow_type AS ENUM('instant', 'hold');
CREATE TYPE feed.risk_score AS ENUM('low', 'high', 'fatal');

CREATE TABLE feed.payment (
  id                                 BIGSERIAL                   NOT NULL,
  event_id                           BIGINT                      NOT NULL,
  event_created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  payment_id                         CHARACTER VARYING           NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  invoice_id                         CHARACTER VARYING           NOT NULL,
  party_id                           CHARACTER VARYING           NOT NULL,
  shop_id                            CHARACTER VARYING           NOT NULL,
  domain_revision                    BIGINT                      NOT NULL,
  party_revision                     BIGINT,
  status                             feed.payment_status           NOT NULL,
  status_cancelled_reason            CHARACTER VARYING,
  status_captured_reason             CHARACTER VARYING,
  status_failed_failure              CHARACTER VARYING,
  amount                             BIGINT                      NOT NULL,
  currency_code                      CHARACTER VARYING           NOT NULL,
  payer_type                         feed.payer_type               NOT NULL,
  payer_payment_tool_type            feed.payment_tool_type        NOT NULL,
  payer_bank_card_token              CHARACTER VARYING,
  payer_bank_card_payment_system     CHARACTER VARYING,
  payer_bank_card_bin                CHARACTER VARYING,
  payer_bank_card_masked_pan         CHARACTER VARYING,
  payer_bank_card_token_provider     CHARACTER VARYING,
  payer_payment_terminal_type        CHARACTER VARYING,
  payer_digital_wallet_provider      CHARACTER VARYING,
  payer_digital_wallet_id            CHARACTER VARYING,
  payer_payment_session_id           CHARACTER VARYING,
  payer_ip_address                   CHARACTER VARYING,
  payer_fingerprint                  CHARACTER VARYING,
  payer_phone_number                 CHARACTER VARYING,
  payer_email                        CHARACTER VARYING,
  payer_customer_id                  CHARACTER VARYING,
  payer_customer_binding_id          CHARACTER VARYING,
  payer_customer_rec_payment_tool_id CHARACTER VARYING,
  context                            BYTEA,
  payment_flow_type                  feed.payment_flow_type        NOT NULL,
  payment_flow_on_hold_expiration    CHARACTER VARYING,
  payment_flow_held_until            TIMESTAMP WITHOUT TIME ZONE,
  risk_score                         feed.risk_score,
  route_provider_id                  INT,
  route_terminal_id                  INT,
  wtime                              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                            BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT payment_pkey PRIMARY KEY (id)
);

CREATE INDEX payment_event_id on feed.payment(event_id);
CREATE INDEX payment_event_created_at on feed.payment(event_created_at);
CREATE INDEX payment_invoice_id on feed.payment(invoice_id);
CREATE INDEX payment_party_id on feed.payment(party_id);
CREATE INDEX payment_status on feed.payment(status);
CREATE INDEX payment_created_at on feed.payment(created_at);

CREATE TYPE feed.cash_flow_account AS ENUM ('merchant', 'provider', 'system', 'external', 'wallet');

CREATE TYPE feed.payment_change_type AS ENUM ('payment', 'refund', 'adjustment', 'payout');

CREATE TYPE feed.adjustment_cash_flow_type AS ENUM ('new_cash_flow', 'old_cash_flow_inverse');

CREATE TABLE feed.cash_flow(
  id                                 BIGSERIAL                   NOT NULL,
  obj_id                             BIGINT                      NOT NULL,
  obj_type                           feed.payment_change_type      NOT NULL,
  adj_flow_type                      feed.adjustment_cash_flow_type,
  source_account_type                feed.cash_flow_account        NOT NULL,
  source_account_type_value          CHARACTER VARYING           NOT NULL,
  source_account_id                  BIGINT                      NOT NULL,
  destination_account_type           feed.cash_flow_account        NOT NULL,
  destination_account_type_value     CHARACTER VARYING           NOT NULL,
  destination_account_id             BIGINT                      NOT NULL,
  amount                             BIGINT                      NOT NULL,
  currency_code                      CHARACTER VARYING           NOT NULL,
  details                            CHARACTER VARYING,
  CONSTRAINT cash_flow_pkey PRIMARY KEY (id)
);

CREATE INDEX cash_flow_idx on feed.cash_flow(obj_id, obj_type);

-- refunds --

CREATE TYPE feed.refund_status AS ENUM ('pending', 'succeeded', 'failed');

CREATE TABLE feed.refund (
  id                                 BIGSERIAL                   NOT NULL,
  event_id                           BIGINT                      NOT NULL,
  event_created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  domain_revision                    BIGINT                      NOT NULL,
  refund_id                          CHARACTER VARYING           NOT NULL,
  payment_id                         CHARACTER VARYING           NOT NULL,
  invoice_id                         CHARACTER VARYING           NOT NULL,
  party_id                           CHARACTER VARYING           NOT NULL,
  shop_id                            CHARACTER VARYING           NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  status                             feed.refund_status            NOT NULL,
  status_failed_failure              CHARACTER VARYING,
  amount                             BIGINT,
  currency_code                      CHARACTER VARYING,
  reason                             CHARACTER VARYING,
  wtime                              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                            BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT refund_pkey PRIMARY KEY (id)
);

CREATE INDEX refund_event_id on feed.refund(event_id);
CREATE INDEX refund_event_created_at on feed.refund(event_created_at);
CREATE INDEX refund_invoice_id on feed.refund(invoice_id);
CREATE INDEX refund_party_id on feed.refund(party_id);
CREATE INDEX refund_status on feed.refund(status);
CREATE INDEX refund_created_at on feed.refund(created_at);

-- adjustments --

CREATE TYPE feed.adjustment_status AS ENUM ('pending', 'captured', 'cancelled');

CREATE TABLE feed.adjustment (
  id                                 BIGSERIAL                   NOT NULL,
  event_id                           BIGINT                      NOT NULL,
  event_created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  domain_revision                    BIGINT                      NOT NULL,
  adjustment_id                      CHARACTER VARYING           NOT NULL,
  payment_id                         CHARACTER VARYING           NOT NULL,
  invoice_id                         CHARACTER VARYING           NOT NULL,
  party_id                           CHARACTER VARYING           NOT NULL,
  shop_id                            CHARACTER VARYING           NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  status                             feed.adjustment_status        NOT NULL,
  status_captured_at                 TIMESTAMP WITHOUT TIME ZONE,
  status_cancelled_at                TIMESTAMP WITHOUT TIME ZONE,
  reason                             CHARACTER VARYING           NOT NULL,
  wtime                              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                            BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT adjustment_pkey PRIMARY KEY (id)
);

CREATE INDEX adjustment_event_id on feed.adjustment(event_id);
CREATE INDEX adjustment_event_created_at on feed.adjustment(event_created_at);
CREATE INDEX adjustment_invoice_id on feed.adjustment(invoice_id);
CREATE INDEX adjustment_party_id on feed.adjustment(party_id);
CREATE INDEX adjustment_status on feed.adjustment(status);
CREATE INDEX adjustment_created_at on feed.adjustment(created_at);

-----------
-- party_mngmnt --
-----------

CREATE TYPE feed.blocking AS ENUM ('unblocked', 'blocked');
CREATE TYPE feed.suspension AS ENUM ('active', 'suspended');

CREATE TABLE feed.party(
  id                                 BIGSERIAL                   NOT NULL,
  event_id                           BIGINT                      NOT NULL,
  event_created_at                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  party_id                           CHARACTER VARYING           NOT NULL,
  contact_info_email                 CHARACTER VARYING           NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  blocking                           feed.blocking                 NOT NULL,
  blocking_unblocked_reason          CHARACTER VARYING,
  blocking_unblocked_since           TIMESTAMP WITHOUT TIME ZONE,
  blocking_blocked_reason            CHARACTER VARYING,
  blocking_blocked_since             TIMESTAMP WITHOUT TIME ZONE,
  suspension                         feed.suspension               NOT NULL,
  suspension_active_since            TIMESTAMP WITHOUT TIME ZONE,
  suspension_suspended_since         TIMESTAMP WITHOUT TIME ZONE,
  revision                           BIGINT                      NOT NULL,
  revision_changed_at                TIMESTAMP WITHOUT TIME ZONE,
  party_meta_set_ns                  CHARACTER VARYING,
  party_meta_set_data_json           CHARACTER VARYING,
  wtime                              TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                            BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT party_pkey PRIMARY KEY (id)
);

CREATE INDEX party_event_id on feed.party(event_id);
CREATE INDEX party_event_created_at on feed.party(event_created_at);
CREATE INDEX party_party_id on feed.party(party_id);
CREATE INDEX party_current on feed.party(current);
CREATE INDEX party_created_at on feed.party(created_at);
CREATE INDEX party_contact_info_email on feed.party(contact_info_email);

-- contract --

CREATE TYPE feed.contract_status AS ENUM ('active', 'terminated', 'expired');
CREATE TYPE feed.representative_document AS ENUM ('articles_of_association', 'power_of_attorney', 'expired');

CREATE TABLE feed.contract(
  id                                                         BIGSERIAL                   NOT NULL,
  event_id                                                   BIGINT                      NOT NULL,
  event_created_at                                           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  contract_id                                                CHARACTER VARYING           NOT NULL,
  party_id                                                   CHARACTER VARYING           NOT NULL,
  payment_institution_id                                     INT,
  created_at                                                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  valid_since                                                TIMESTAMP WITHOUT TIME ZONE,
  valid_until                                                TIMESTAMP WITHOUT TIME ZONE,
  status                                                     feed.contract_status          NOT NULL,
  status_terminated_at                                       TIMESTAMP WITHOUT TIME ZONE,
  terms_id                                                   INT                         NOT NULL,
  legal_agreement_signed_at                                  TIMESTAMP WITHOUT TIME ZONE,
  legal_agreement_id                                         CHARACTER VARYING,
  legal_agreement_valid_until                                TIMESTAMP WITHOUT TIME ZONE,
  report_act_schedule_id                                     INT,
  report_act_signer_position                                 CHARACTER VARYING,
  report_act_signer_full_name                                CHARACTER VARYING,
  report_act_signer_document                                 feed.representative_document,
  report_act_signer_doc_power_of_attorney_signed_at          TIMESTAMP WITHOUT TIME ZONE,
  report_act_signer_doc_power_of_attorney_legal_agreement_id CHARACTER VARYING,
  report_act_signer_doc_power_of_attorney_valid_until        TIMESTAMP WITHOUT TIME ZONE,
  contractor_id                                              CHARACTER VARYING,
  wtime                                                      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                                    BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT contract_pkey PRIMARY KEY (id)
);

CREATE INDEX contract_event_id on feed.contract(event_id);
CREATE INDEX contract_event_created_at on feed.contract(event_created_at);
CREATE INDEX contract_contract_id on feed.contract(contract_id);
CREATE INDEX contract_party_id on feed.contract(party_id);
CREATE INDEX contract_created_at on feed.contract(created_at);

CREATE TABLE feed.contract_adjustment(
  id                                 BIGSERIAL                   NOT NULL,
  cntrct_id                          BIGINT                      NOT NULL,
  contract_adjustment_id             CHARACTER VARYING           NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  valid_since                        TIMESTAMP WITHOUT TIME ZONE,
  valid_until                        TIMESTAMP WITHOUT TIME ZONE,
  terms_id                           INT                         NOT NULL,
  CONSTRAINT contract_adjustment_pkey PRIMARY KEY (id),
  CONSTRAINT fk_adjustment_to_contract FOREIGN KEY (cntrct_id) REFERENCES feed.contract(id)
);

CREATE INDEX contract_adjustment_idx on feed.contract_adjustment(cntrct_id);

CREATE TYPE feed.payout_tool_info AS ENUM ('russian_bank_account', 'international_bank_account');

CREATE TABLE feed.payout_tool(
  id                                                 BIGSERIAL                   NOT NULL,
  cntrct_id                                          BIGINT                      NOT NULL,
  payout_tool_id                                     CHARACTER VARYING           NOT NULL,
  created_at                                         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  currency_code                                      CHARACTER VARYING           NOT NULL,
  payout_tool_info                                   feed.payout_tool_info         NOT NULL,
  payout_tool_info_russian_bank_account              CHARACTER VARYING,
  payout_tool_info_russian_bank_name                 CHARACTER VARYING,
  payout_tool_info_russian_bank_post_account         CHARACTER VARYING,
  payout_tool_info_russian_bank_bik                  CHARACTER VARYING,
  payout_tool_info_international_bank_account_holder CHARACTER VARYING,
  payout_tool_info_international_bank_name           CHARACTER VARYING,
  payout_tool_info_international_bank_address        CHARACTER VARYING,
  payout_tool_info_international_bank_iban           CHARACTER VARYING,
  payout_tool_info_international_bank_bic            CHARACTER VARYING,
  payout_tool_info_international_bank_local_code     CHARACTER VARYING,
  CONSTRAINT payout_tool_pkey PRIMARY KEY (id),
  CONSTRAINT fk_payout_tool_to_contract FOREIGN KEY (cntrct_id) REFERENCES feed.contract(id)
);

CREATE INDEX payout_tool_idx on feed.payout_tool(cntrct_id);

-- contractor --

CREATE TYPE feed.contractor_type AS ENUM ('registered_user', 'legal_entity', 'private_entity');
CREATE TYPE feed.legal_entity AS ENUM ('russian_legal_entity', 'international_legal_entity');
CREATE TYPE feed.private_entity AS ENUM ('russian_private_entity');

CREATE TABLE feed.contractor(
  id                                              BIGSERIAL                   NOT NULL,
  event_id                                        BIGINT                      NOT NULL,
  event_created_at                                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  party_id                                        CHARACTER VARYING           NOT NULL,
  contractor_id                                   CHARACTER VARYING           NOT NULL,
  type                                            feed.contractor_type          NOT NULL,
  identificational_level                          CHARACTER VARYING,
  registered_user_email                           CHARACTER VARYING,
  legal_entity                                    feed.legal_entity,
  russian_legal_entity_registered_name            CHARACTER VARYING,
  russian_legal_entity_registered_number          CHARACTER VARYING,
  russian_legal_entity_inn                        CHARACTER VARYING,
  russian_legal_entity_actual_address             CHARACTER VARYING,
  russian_legal_entity_post_address               CHARACTER VARYING,
  russian_legal_entity_representative_position    CHARACTER VARYING,
  russian_legal_entity_representative_full_name   CHARACTER VARYING,
  russian_legal_entity_representative_document    CHARACTER VARYING,
  russian_legal_entity_russian_bank_account       CHARACTER VARYING,
  russian_legal_entity_russian_bank_name          CHARACTER VARYING,
  russian_legal_entity_russian_bank_post_account  CHARACTER VARYING,
  russian_legal_entity_russian_bank_bik           CHARACTER VARYING,
  international_legal_entity_legal_name           CHARACTER VARYING,
  international_legal_entity_trading_name         CHARACTER VARYING,
  international_legal_entity_registered_address   CHARACTER VARYING,
  international_legal_entity_actual_address       CHARACTER VARYING,
  international_legal_entity_registered_number    CHARACTER VARYING,
  private_entity                                  feed.private_entity,
  russian_private_entity_first_name               CHARACTER VARYING,
  russian_private_entity_second_name              CHARACTER VARYING,
  russian_private_entity_middle_name              CHARACTER VARYING,
  russian_private_entity_phone_number             CHARACTER VARYING,
  russian_private_entity_email                    CHARACTER VARYING,
  wtime                                           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                         BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT contractor_pkey PRIMARY KEY (id)
);

CREATE INDEX contractor_event_id on feed.contractor(event_id);
CREATE INDEX contractor_event_created_at on feed.contractor(event_created_at);
CREATE INDEX contractor_contractor_id on feed.contractor(contractor_id);
CREATE INDEX contractor_party_id on feed.contractor(party_id);

-- shop --

CREATE TABLE feed.shop(
  id                                              BIGSERIAL                   NOT NULL,
  event_id                                        BIGINT                      NOT NULL,
  event_created_at                                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  party_id                                        CHARACTER VARYING           NOT NULL,
  shop_id                                         CHARACTER VARYING           NOT NULL,
  created_at                                      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  blocking                                        feed.blocking                 NOT NULL,
  blocking_unblocked_reason                       CHARACTER VARYING,
  blocking_unblocked_since                        TIMESTAMP WITHOUT TIME ZONE,
  blocking_blocked_reason                         CHARACTER VARYING,
  blocking_blocked_since                          TIMESTAMP WITHOUT TIME ZONE,
  suspension                                      feed.suspension               NOT NULL,
  suspension_active_since                         TIMESTAMP WITHOUT TIME ZONE,
  suspension_suspended_since                      TIMESTAMP WITHOUT TIME ZONE,
  details_name                                    CHARACTER VARYING           NOT NULL,
  details_description                             CHARACTER VARYING,
  location_url                                    CHARACTER VARYING           NOT NULL,
  category_id                                     INT                         NOT NULL,
  account_currency_code                           CHARACTER VARYING,
  account_settlement                              BIGINT,
  account_guarantee                               BIGINT,
  account_payout                                  BIGINT,
  contract_id                                     CHARACTER VARYING           NOT NULL,
  payout_tool_id                                  CHARACTER VARYING,
  payout_schedule_id                              INT,
  wtime                                           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                         BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT shop_pkey PRIMARY KEY (id)
);

CREATE INDEX shop_event_id on feed.shop(event_id);
CREATE INDEX shop_event_created_at on feed.shop(event_created_at);
CREATE INDEX shop_shop_id on feed.shop(shop_id);
CREATE INDEX shop_party_id on feed.shop(party_id);
CREATE INDEX shop_created_at on feed.shop(created_at);

-- payout --

CREATE TYPE feed.payout_status AS ENUM ('unpaid', 'paid', 'cancelled', 'confirmed');
CREATE TYPE feed.payout_paid_status_details AS ENUM ('card_details', 'account_details');
CREATE TYPE feed.user_type AS ENUM ('internal_user', 'external_user', 'service_user');
CREATE TYPE feed.payout_type AS ENUM ('bank_card', 'bank_account');
CREATE TYPE feed.payout_account_type AS ENUM ('russian_payout_account', 'international_payout_account');

CREATE TABLE feed.payout(
  id                                                         BIGSERIAL                   NOT NULL,
  event_id                                                   BIGINT                      NOT NULL,
  event_created_at                                           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  payout_id                                                  CHARACTER VARYING           NOT NULL,
  party_id                                                   CHARACTER VARYING           NOT NULL,
  shop_id                                                    CHARACTER VARYING           NOT NULL,
  contract_id                                                CHARACTER VARYING           NOT NULL,
  created_at                                                 TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  status                                                     feed.payout_status            NOT NULL,
  status_paid_details                                        feed.payout_paid_status_details,
  status_paid_details_card_provider_name                     CHARACTER VARYING,
  status_paid_details_card_provider_transaction_id           CHARACTER VARYING,
  status_cancelled_user_info_id                              CHARACTER VARYING,
  status_cancelled_user_info_type                            feed.user_type,
  status_cancelled_details                                   CHARACTER VARYING,
  status_confirmed_user_info_id                              CHARACTER VARYING,
  status_confirmed_user_info_type                            feed.user_type,
  type                                                       feed.payout_type              NOT NULL,
  type_card_token                                            CHARACTER VARYING,
  type_card_payment_system                                   CHARACTER VARYING,
  type_card_bin                                              CHARACTER VARYING,
  type_card_masked_pan                                       CHARACTER VARYING,
  type_card_token_provider                                   CHARACTER VARYING,
  type_account_type                                          feed.payout_account_type,
  type_account_russian_account                               CHARACTER VARYING,
  type_account_russian_bank_name                             CHARACTER VARYING,
  type_account_russian_bank_post_account                     CHARACTER VARYING,
  type_account_russian_bank_bik                              CHARACTER VARYING,
  type_account_russian_inn                                   CHARACTER VARYING,
  type_account_international_account_holder                  CHARACTER VARYING,
  type_account_international_bank_name                       CHARACTER VARYING,
  type_account_international_bank_address                    CHARACTER VARYING,
  type_account_international_iban                            CHARACTER VARYING,
  type_account_international_bic                             CHARACTER VARYING,
  type_account_international_local_bank_code                 CHARACTER VARYING,
  type_account_international_legal_entity_legal_name         CHARACTER VARYING,
  type_account_international_legal_entity_trading_name       CHARACTER VARYING,
  type_account_international_legal_entity_registered_address CHARACTER VARYING,
  type_account_international_legal_entity_actual_address     CHARACTER VARYING,
  type_account_international_legal_entity_registered_number  CHARACTER VARYING,
  type_account_purpose                                       CHARACTER VARYING,
  type_account_legal_agreement_signed_at                     TIMESTAMP WITHOUT TIME ZONE,
  type_account_legal_agreement_id                            CHARACTER VARYING,
  type_account_legal_agreement_valid_until                   TIMESTAMP WITHOUT TIME ZONE,
  initiator_id                                               CHARACTER VARYING           NOT NULL,
  initiator_type                                             feed.user_type                NOT NULL,
  wtime                                                      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                                                    BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT payout_pkey PRIMARY KEY (id)
);

CREATE INDEX payout_event_id on feed.payout(event_id);
CREATE INDEX payout_event_created_at on feed.payout(event_created_at);
CREATE INDEX payout_payout_id on feed.payout(payout_id);
CREATE INDEX payout_party_id on feed.payout(party_id);
CREATE INDEX payout_created_at on feed.payout(created_at);
CREATE INDEX payout_status on feed.payout(status);

CREATE TABLE feed.payout_summary(
  id                     BIGSERIAL                   NOT NULL,
  pyt_id                 BIGINT                      NOT NULL,
  amount                 BIGINT                      NOT NULL,
  fee                    BIGINT                      NOT NULL,
  currency_code          CHARACTER VARYING           NOT NULL,
  from_time              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  to_time                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  operation_type         CHARACTER VARYING           NOT NULL,
  count                  INT                         NOT NULL,
  CONSTRAINT payout_summary_pkey PRIMARY KEY (id),
  CONSTRAINT fk_summary_to_payout FOREIGN KEY (pyt_id) REFERENCES feed.payout(id)
);

CREATE INDEX payout_summary_idx on feed.payout_summary(pyt_id);

/******************************************************************************************************************/

ALTER TABLE feed.contract ADD COLUMN revision BIGINT NOT NULL;
ALTER TABLE feed.contractor ADD COLUMN revision BIGINT NOT NULL;
ALTER TABLE feed.shop ADD COLUMN revision BIGINT NOT NULL;

/******************************************************************************************************************/
/******************************************************************************************************************/

ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_bank_number CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_bank_aba_rtn CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_bank_country_code CHARACTER VARYING;

ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_account CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_name CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_address CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_bic CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_iban CHARACTER VARYING;
  ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_number CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_aba_rtn CHARACTER VARYING;
ALTER TABLE feed.payout_tool
  ADD COLUMN payout_tool_info_international_correspondent_bank_country_code CHARACTER VARYING;

ALTER TABLE feed.payout
  ADD COLUMN type_account_international_bank_number CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_bank_aba_rtn CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_bank_country_code CHARACTER VARYING;

ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_number CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_account CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_name CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_address CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_bic CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_iban CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_aba_rtn CHARACTER VARYING;
ALTER TABLE feed.payout
  ADD COLUMN type_account_international_correspondent_bank_country_code CHARACTER VARYING;


/******************************************************************************************************************/

CREATE TABLE feed.category(
  id                       BIGSERIAL NOT NULL,
  version_id               BIGINT NOT NULL,
  category_ref_id          INT NOT NULL,
  name                     CHARACTER VARYING NOT NULL,
  description              CHARACTER VARYING NOT NULL,
  type                     CHARACTER VARYING,
  wtime                    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                  BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT category_pkey PRIMARY KEY (id)
);

CREATE INDEX category_version_id on feed.category(version_id);
CREATE INDEX category_idx on feed.category(category_ref_id);

--currency--
CREATE TABLE feed.currency(
  id                       BIGSERIAL NOT NULL,
  version_id               BIGINT NOT NULL,
  currency_ref_id          CHARACTER VARYING NOT NULL,
  name                     CHARACTER VARYING NOT NULL,
  symbolic_code            CHARACTER VARYING NOT NULL,
  numeric_code             SMALLINT NOT NULL,
  exponent                 SMALLINT NOT NULL,
  wtime                    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                  BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT currency_pkey PRIMARY KEY (id)
);

CREATE INDEX currency_version_id on feed.currency(version_id);
CREATE INDEX currency_idx on feed.currency(currency_ref_id);

--calendar--
CREATE TABLE feed.calendar(
  id                       BIGSERIAL NOT NULL,
  version_id               BIGINT NOT NULL,
  calendar_ref_id          INT NOT NULL,
  name                     CHARACTER VARYING NOT NULL,
  description              CHARACTER VARYING,
  timezone                 CHARACTER VARYING NOT NULL,
  holidays_json            CHARACTER VARYING NOT NULL,
  first_day_of_week        INT,
  wtime                    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                  BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT calendar_pkey PRIMARY KEY (id)
);

CREATE INDEX calendar_version_id on feed.calendar(version_id);
CREATE INDEX calendar_idx on feed.calendar(calendar_ref_id);

--provider--
CREATE TABLE feed.provider(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  provider_ref_id                INT NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  proxy_ref_id                   INT NOT NULL,
  proxy_additional_json          CHARACTER VARYING NOT NULL,
  terminal_json                  CHARACTER VARYING NOT NULL,
  abs_account                    CHARACTER VARYING NOT NULL,
  payment_terms_json             CHARACTER VARYING,
  recurrent_paytool_terms_json   CHARACTER VARYING,
  accounts_json                  CHARACTER VARYING,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT provider_pkey PRIMARY KEY (id)
);

CREATE INDEX provider_version_id on feed.provider(version_id);
CREATE INDEX provider_idx on feed.provider(provider_ref_id);

--terminal--
CREATE TABLE feed.terminal(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  terminal_ref_id                INT NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  options_json                   CHARACTER VARYING,
  risk_coverage                  CHARACTER VARYING NOT NULL,
  terms_json                     CHARACTER VARYING,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT terminal_pkey PRIMARY KEY (id)
);

CREATE INDEX terminal_version_id on feed.terminal(version_id);
CREATE INDEX terminal_idx on feed.terminal(terminal_ref_id);

--payment_method--
CREATE TYPE feed.payment_method_type AS ENUM('bank_card', 'payment_terminal', 'digital_wallet', 'tokenized_bank_card');

CREATE TABLE feed.payment_method(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  payment_method_ref_id          CHARACTER VARYING NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  type                           feed.payment_method_type NOT NULL,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT payment_method_pkey PRIMARY KEY (id)
);

CREATE INDEX payment_method_version_id on feed.payment_method(version_id);
CREATE INDEX payment_method_idx on feed.payment_method(payment_method_ref_id);

--payout_method--
CREATE TABLE feed.payout_method(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  payout_method_ref_id           CHARACTER VARYING NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT payout_method_pkey PRIMARY KEY (id)
);

CREATE INDEX payout_method_version_id on feed.payout_method(version_id);
CREATE INDEX payout_method_idx on feed.payout_method(payout_method_ref_id);

--payment_institution--
CREATE TABLE feed.payment_institution(
  id                                    BIGSERIAL NOT NULL,
  version_id                            BIGINT NOT NULL,
  payment_institution_ref_id            INT NOT NULL,
  name                                  CHARACTER VARYING NOT NULL,
  description                           CHARACTER VARYING,
  calendar_ref_id                       INT,
  system_account_set_json               CHARACTER VARYING NOT NULL,
  default_contract_template_json        CHARACTER VARYING NOT NULL,
  default_wallet_contract_template_json CHARACTER VARYING,
  providers_json                        CHARACTER VARYING NOT NULL,
  inspector_json                        CHARACTER VARYING NOT NULL,
  realm                                 CHARACTER VARYING NOT NULL,
  residences_json                       CHARACTER VARYING NOT NULL,
  wtime                                 TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                               BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT payment_institution_pkey PRIMARY KEY (id)
);

CREATE INDEX payment_institution_version_id on feed.payment_institution(version_id);
CREATE INDEX payment_institution_idx on feed.payment_institution(payment_institution_ref_id);

--inspector--
CREATE TABLE feed.inspector(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  inspector_ref_id               INT NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  proxy_ref_id                   INT NOT NULL,
  proxy_additional_json          CHARACTER VARYING NOT NULL,
  fallback_risk_score            CHARACTER VARYING,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT inspector_pkey PRIMARY KEY (id)
);

CREATE INDEX inspector_version_id on feed.inspector(version_id);
CREATE INDEX inspector_idx on feed.inspector(inspector_ref_id);

--proxy--
CREATE TABLE feed.proxy(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  proxy_ref_id                   INT NOT NULL,
  name                           CHARACTER VARYING NOT NULL,
  description                    CHARACTER VARYING NOT NULL,
  url                            CHARACTER VARYING NOT NULL,
  options_json                   CHARACTER VARYING NOT NULL,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT proxy_pkey PRIMARY KEY (id)
);

CREATE INDEX proxy_version_id on feed.proxy(version_id);
CREATE INDEX proxy_idx on feed.proxy(proxy_ref_id);

--term_set_hierarchy--
CREATE TABLE feed.term_set_hierarchy(
  id                             BIGSERIAL NOT NULL,
  version_id                     BIGINT NOT NULL,
  term_set_hierarchy_ref_id      INT NOT NULL,
  name                           CHARACTER VARYING,
  description                    CHARACTER VARYING,
  parent_terms_ref_id            INT,
  term_sets_json                 CHARACTER VARYING NOT NULL,
  wtime                          TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() at time zone 'utc'),
  current                        BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT term_set_hierarchy_pkey PRIMARY KEY (id)
);

CREATE INDEX term_set_hierarchy_version_id on feed.term_set_hierarchy(version_id);
CREATE INDEX term_set_hierarchy_idx on feed.term_set_hierarchy(term_set_hierarchy_ref_id);

/******************************************************************************************************************/

CREATE TYPE feed.session_target_status AS ENUM('processed', 'captured', 'cancelled', 'refunded');
CREATE TYPE feed.session_change_payload AS ENUM('session_started', 'session_finished', 'session_suspended', 'session_activated', 'session_transaction_bound', 'session_proxy_state_changed', 'session_interaction_requested');
CREATE TYPE feed.session_change_payload_finished_result AS ENUM('succeeded', 'failed');

ALTER TABLE feed.payment ADD COLUMN session_target feed.session_target_status;
ALTER TABLE feed.payment ADD COLUMN session_payload feed.session_change_payload;
ALTER TABLE feed.payment ADD COLUMN session_payload_finished_result feed.session_change_payload_finished_result;
ALTER TABLE feed.payment ADD COLUMN session_payload_finished_result_failed_failure_json CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN session_payload_suspended_tag CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN session_payload_transaction_bound_trx_id CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN session_payload_transaction_bound_trx_timestamp TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE feed.payment ADD COLUMN session_payload_transaction_bound_trx_extra_json CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN session_payload_proxy_state_changed_proxy_state BYTEA;
ALTER TABLE feed.payment ADD COLUMN session_payload_interaction_requested_interaction_json CHARACTER VARYING;

ALTER TABLE feed.refund ADD COLUMN session_target feed.session_target_status;
ALTER TABLE feed.refund ADD COLUMN session_payload feed.session_change_payload;
ALTER TABLE feed.refund ADD COLUMN session_payload_finished_result feed.session_change_payload_finished_result;
ALTER TABLE feed.refund ADD COLUMN session_payload_finished_result_failed_failure_json CHARACTER VARYING;
ALTER TABLE feed.refund ADD COLUMN session_payload_suspended_tag CHARACTER VARYING;
ALTER TABLE feed.refund ADD COLUMN session_payload_transaction_bound_trx_id CHARACTER VARYING;
ALTER TABLE feed.refund ADD COLUMN session_payload_transaction_bound_trx_timestamp TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE feed.refund ADD COLUMN session_payload_transaction_bound_trx_extra_json CHARACTER VARYING;
ALTER TABLE feed.refund ADD COLUMN session_payload_proxy_state_changed_proxy_state BYTEA;
ALTER TABLE feed.refund ADD COLUMN session_payload_interaction_requested_interaction_json CHARACTER VARYING;

/******************************************************************************************************************/

ALTER TABLE feed.payment ADD COLUMN fee BIGINT;
ALTER TABLE feed.payment ADD COLUMN provider_fee BIGINT;
ALTER TABLE feed.payment ADD COLUMN external_fee BIGINT;
ALTER TABLE feed.payment ADD COLUMN guarantee_deposit BIGINT;

ALTER TABLE feed.refund ADD COLUMN fee BIGINT;
ALTER TABLE feed.refund ADD COLUMN provider_fee BIGINT;
ALTER TABLE feed.refund ADD COLUMN external_fee BIGINT;

ALTER TABLE feed.adjustment ADD COLUMN fee BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN provider_fee BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN external_fee BIGINT;

/******************************************************************************************************************/

ALTER TABLE feed.payment ADD COLUMN make_recurrent BOOL;
ALTER TABLE feed.payment ADD COLUMN payer_recurrent_parent_invoice_id CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN payer_recurrent_parent_payment_id CHARACTER VARYING;
ALTER TABLE feed.payment ADD COLUMN recurrent_intention_token CHARACTER VARYING;

/******************************************************************************************************************/

ALTER TABLE feed.refund ADD COLUMN party_revision BIGINT;
ALTER TABLE feed.adjustment ADD COLUMN party_revision BIGINT;

/******************************************************************************************************************/

ALTER TABLE feed.payment DROP COLUMN session_target;
ALTER TABLE feed.payment DROP COLUMN session_payload;
ALTER TABLE feed.payment DROP COLUMN session_payload_finished_result;
ALTER TABLE feed.payment DROP COLUMN session_payload_finished_result_failed_failure_json;
ALTER TABLE feed.payment DROP COLUMN session_payload_suspended_tag;
ALTER TABLE feed.payment DROP COLUMN session_payload_transaction_bound_trx_timestamp;
ALTER TABLE feed.payment DROP COLUMN session_payload_proxy_state_changed_proxy_state;
ALTER TABLE feed.payment DROP COLUMN session_payload_interaction_requested_interaction_json;

ALTER TABLE feed.refund DROP COLUMN session_target;
ALTER TABLE feed.refund DROP COLUMN session_payload;
ALTER TABLE feed.refund DROP COLUMN session_payload_finished_result;
ALTER TABLE feed.refund DROP COLUMN session_payload_finished_result_failed_failure_json;
ALTER TABLE feed.refund DROP COLUMN session_payload_suspended_tag;
ALTER TABLE feed.refund DROP COLUMN session_payload_transaction_bound_trx_timestamp;
ALTER TABLE feed.refund DROP COLUMN session_payload_proxy_state_changed_proxy_state;
ALTER TABLE feed.refund DROP COLUMN session_payload_interaction_requested_interaction_json;

DROP TYPE IF EXISTS feed.session_target_status;
DROP TYPE IF EXISTS feed.session_change_payload;
DROP TYPE IF EXISTS feed.session_change_payload_finished_result;
