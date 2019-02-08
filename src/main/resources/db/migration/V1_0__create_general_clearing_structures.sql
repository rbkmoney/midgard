CREATE SCHEMA IF NOT EXISTS midgard;

/***************************************************************************/
CREATE TYPE midgard.transaction_clearing_state AS ENUM ('CREATED', 'READY', 'ACTIVE', 'FINISHED', 'FAILED');

CREATE TABLE midgard.clearing_transaction (
  event_id                        BIGINT                        NOT NULL,
  invoice_id                      CHARACTER VARYING             NOT NULL,
  payment_id                      CHARACTER VARYING             NOT NULL,
  provider_id                     INTEGER                       NOT NULL,
  transaction_id                  CHARACTER VARYING             NOT NULL,
  transaction_date                TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
  transaction_amount              BIGINT                        NOT NULL,
  transaction_currency            VARCHAR(3)                    NOT NULL,
  transaction_clearing_state      transaction_clearing_state    NOT NULL,
  party_id                        CHARACTER VARYING             NOT NULL,
  shop_id                         CHARACTER VARYING             NOT NULL,
  mcc                             INTEGER                       NULL,
  payer_bank_card_token           CHARACTER VARYING             NULL,
  payer_bank_card_payment_system  CHARACTER VARYING             NULL,
  payer_bank_card_bin             CHARACTER VARYING             NULL,
  payer_bank_card_masked_pan      CHARACTER VARYING             NULL,
  payer_bank_card_token_provider  CHARACTER VARYING             NULL,
  extra                           CHARACTER VARYING             NULL,
  comment                         CHARACTER VARYING             NULL,
  clearing_id                     BIGINT                        NULL,
  last_act_time                   TIMESTAMP WITHOUT TIME ZONE   NOT NULL  DEFAULT (now() at time zone 'utc'),

  CONSTRAINT clearing_transaction_PK PRIMARY KEY (event_id)
);

CREATE INDEX clearing_transactions_id_idx ON midgard.clearing_transaction (transaction_id);

CREATE INDEX clearing_transactions_event_id_idx ON midgard.clearing_transaction (invoice_id, payment_id);

CREATE INDEX clearing_transactions_date_idx ON midgard.clearing_transaction (transaction_date, provider_id);


/***************************************************************************/
CREATE TABLE midgard.clearing_refund (
  event_id                           BIGINT                               NOT NULL, -- есть в транзакциях
  domain_revision                    BIGINT                               NOT NULL,
  refund_id                          CHARACTER VARYING                    NOT NULL,
  payment_id                         CHARACTER VARYING                    NOT NULL,
  invoice_id                         CHARACTER VARYING                    NOT NULL,
  transaction_id                     CHARACTER VARYING                    NOT NULL,
  party_id                           CHARACTER VARYING                    NOT NULL,
  shop_id                            CHARACTER VARYING                    NOT NULL,
  created_at                         TIMESTAMP WITHOUT TIME ZONE          NOT NULL,
  clearing_state                     midgard.transaction_clearing_state   NOT NULL,
  amount                             BIGINT                               NULL,
  currency_code                      CHARACTER VARYING                    NULL,
  reason                             CHARACTER VARYING                    NULL,
  extra                              CHARACTER VARYING                    NULL,
  CONSTRAINT clearing_refund_pkey PRIMARY KEY (event_id)
);

CREATE INDEX clearing_refund_state_idx ON midgard.clearing_refund (payment_id, invoice_id, event_id);


/***************************************************************************/
CREATE TYPE midgard.cash_flow_account AS ENUM ('merchant', 'provider', 'system', 'external', 'wallet');

CREATE TYPE midgard.payment_change_type AS ENUM ('payment', 'refund', 'adjustment', 'payout');

CREATE TABLE midgard.clearing_transaction_cash_flow(
  id                                 BIGSERIAL                        NOT NULL,
  source_event_id                    BIGINT                           NOT NULL,
  obj_type                           midgard.payment_change_type      NOT NULL,
  source_account_type                midgard.cash_flow_account        NOT NULL,
  source_account_type_value          CHARACTER VARYING                NOT NULL,
  source_account_id                  BIGINT                           NOT NULL,
  destination_account_type           midgard.cash_flow_account        NOT NULL,
  destination_account_type_value     CHARACTER VARYING                NOT NULL,
  destination_account_id             BIGINT                           NOT NULL,
  amount                             BIGINT                           NOT NULL,
  currency_code                      CHARACTER VARYING                NOT NULL,
  details                            CHARACTER VARYING                NULL,
  CONSTRAINT cash_flow_pkey PRIMARY KEY (source_event_id, id)
);


/***************************************************************************/
/**        Список клиринговых событий, полученных от внешних систем        */
/***************************************************************************/
CREATE TYPE midgard.clearing_event_status AS ENUM ('STARTED', 'EXECUTE', 'COMPLETE', 'FAILED');

CREATE TABLE midgard.clearing_event_info (
  id           BIGSERIAL                     NOT NULL,
  event_id     BIGINT                        NOT NULL,
  date         TIMESTAMP WITHOUT TIME ZONE   NOT NULL DEFAULT (now() at time zone 'utc'),
  provider_id  INTEGER                       NOT NULL,
  status       clearing_event_status         NOT NULL,
  CONSTRAINT clearing_event_info_PK PRIMARY KEY (id)
);

CREATE INDEX clearing_event_idx ON midgard.clearing_event_info (provider_id, date desc, id);


/***************************************************************************/
/**        Список транзакций, участвующих в клиринговом событии            */
/***************************************************************************/
CREATE TYPE midgard.clearing_trx_type AS ENUM ('PAYMENT', 'REFUND');

CREATE TABLE midgard.clearing_event_transaction_info (
  clearing_id       BIGINT                        NOT NULL,
  transaction_id    VARCHAR(100)                  NOT NULL,
  transaction_type  clearing_trx_type             NOT NULL,
  row_number        INTEGER                       NOT NULL,
  CONSTRAINT clearing_event_transaction_info_PK PRIMARY KEY (clearing_id, transaction_id)
);

CREATE INDEX clearing_trx_event_info_idx ON midgard.clearing_event_transaction_info (clearing_id, row_number);

CREATE INDEX clearing_trx_event_info_by_type_idx ON midgard.clearing_event_transaction_info (clearing_id, transaction_type);


/***************************************************************************/
/**        Список сбойных транзакций в рамках клирингового эвента          */
/***************************************************************************/
CREATE TABLE midgard.failure_transaction (
  clearing_id            BIGSERIAL                     NOT NULL,
  transaction_id         VARCHAR(100)                  NOT NULL,
  act_time               TIMESTAMP WITHOUT TIME ZONE   NOT NULL DEFAULT (now() at time zone 'utc'),
  reason                 VARCHAR(500)                  NULL,
  CONSTRAINT failure_transaction_PK PRIMARY KEY (clearing_id, transaction_id)
);

CREATE INDEX failure_transaction_idx ON midgard.failure_transaction (transaction_id);
