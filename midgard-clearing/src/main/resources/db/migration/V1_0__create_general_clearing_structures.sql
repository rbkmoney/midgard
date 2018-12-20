CREATE SCHEMA IF NOT EXISTS midgard;

/***************************************************************************/
CREATE TYPE midgard.merchant_state AS ENUM ('OPEN', 'CLOSE');

CREATE TABLE midgard.clearing_merchant (
  merchant_id                  VARCHAR(15)                   NOT NULL,
  merchant_name                VARCHAR(32)                   NOT NULL,
  merchant_address             VARCHAR(64)                   NOT NULL,
  merchant_country             VARCHAR(3)                    NOT NULL,
  merchant_city                VARCHAR(21)                   NOT NULL,
  merchant_postal_code         VARCHAR(10)                   NOT NULL,
  status                       merchant_state                NOT NULL,
  valid_from                   TIMESTAMP WITHOUT TIME ZONE   NOT NULL  DEFAULT (now() at time zone 'utc'),
  valid_to                     TIMESTAMP WITHOUT TIME ZONE   NULL,
  CONSTRAINT clearing_merchant_PK PRIMARY KEY (merchant_id)
);

CREATE INDEX clearing_merchant_id_idx ON midgard.clearing_merchant (merchant_id);


/***************************************************************************/
CREATE TYPE midgard.transaction_clearing_state AS ENUM ('CREATED', 'READY', 'ACTIVE', 'FINISHED', 'FAILED');

CREATE TABLE midgard.clearing_transaction (
  invoice_id                      VARCHAR(100)                  NOT NULL,
  doc_id                          VARCHAR(100)                  NOT NULL,
  provider_id                     VARCHAR(100)                  NOT NULL,
  transaction_id                  VARCHAR(100)                  NULL,
  transaction_date                TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
  transaction_amount              BIGINT                        NOT NULL,
  transaction_currency            VARCHAR(3)                    NOT NULL,
  transaction_type                VARCHAR(2)                    NULL,
  transaction_clearing_state      transaction_clearing_state    NOT NULL,
  merchant_id                     VARCHAR(15)                   NOT NULL,
  terminal_id                     VARCHAR(8)                    NOT NULL,
  mcc                             INTEGER                       NOT NULL,
  payer_bank_card_token           CHARACTER VARYING             NULL,
  payer_bank_card_payment_system  CHARACTER VARYING             NULL,
  payer_bank_card_bin             CHARACTER VARYING             NULL,
  payer_bank_card_masked_pan      CHARACTER VARYING             NULL,
  payer_bank_card_token_provider  CHARACTER VARYING             NULL,
  extra                           CHARACTER VARYING             NULL,
  fee                             BIGINT                        NULL,
  provider_fee                    BIGINT                        NULL,
  external_fee                    BIGINT                        NULL,
  clearing_id                     BIGINT                        NULL,
  last_act_time                   TIMESTAMP WITHOUT TIME ZONE   NOT NULL  DEFAULT (now() at time zone 'utc'),

  CONSTRAINT clearing_transaction_PK PRIMARY KEY (invoice_id, doc_id)
);

CREATE INDEX clearing_transactions_id_idx ON midgard.clearing_transaction (transaction_id);

CREATE INDEX clearing_transactions_date_idx ON midgard.clearing_transaction (transaction_date, provider_id);

/***************************************************************************/
--TODO: проверить необходимость конфига. Что нужно будет конфигурировать?
CREATE TABLE midgard.config (
  name        VARCHAR(150)   NOT NULL,
  value       VARCHAR(150)   NOT NULL,
  comment     VARCHAR(500)   NOT NULL,
  CONSTRAINT config_PK PRIMARY KEY (name)
);

/***************************************************************************/
CREATE TYPE midgard.clearing_event_state AS ENUM ('STARTED', 'EXECUTE', 'SUCCESSFULLY', 'FAILED');

CREATE TABLE midgard.clearing_event (
  id           BIGSERIAL                     NOT NULL,
  event_id     BIGINT                        NOT NULL,
  date         TIMESTAMP WITHOUT TIME ZONE   NOT NULL DEFAULT (now() at time zone 'utc'),
  provider_id  VARCHAR(100)                  NOT NULL,
  state        clearing_event_state          NOT NULL,
  CONSTRAINT clearing_event_PK PRIMARY KEY (id)
);

CREATE INDEX clearing_event_idx ON midgard.clearing_event (provider_id, date desc, id);


/***************************************************************************/
CREATE TYPE midgard.clearing_trx_event_state AS ENUM ('PROCESSED', 'REFUSED');

CREATE TABLE midgard.clearing_transaction_event_info (
  clearing_id     BIGINT                        NOT NULL,
  transaction_id  VARCHAR(100)                  NOT NULL,
  merchant_id     VARCHAR(100)                  NULL,
  state           clearing_trx_event_state      NOT NULL,
  row_number      INTEGER                       NOT NULL,
  CONSTRAINT clearing_event_info_PK PRIMARY KEY (clearing_id, transaction_id)
);

CREATE INDEX clearing_transaction_event_info_idx ON midgard.clearing_transaction_event_info (clearing_id, row_number);


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
