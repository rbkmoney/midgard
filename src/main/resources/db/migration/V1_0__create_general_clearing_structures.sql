CREATE SCHEMA IF NOT EXISTS midgard;

/***************************************************************************/
CREATE TABLE midgard.account_type (
  id          VARCHAR(2)     NOT NULL,
  type_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT account_type_PK PRIMARY KEY (id)
);

INSERT INTO midgard.account_type VALUES ('00', 'Default – unspecified');
INSERT INTO midgard.account_type VALUES ('10', 'Savings account');
INSERT INTO midgard.account_type VALUES ('20', 'Check account');
INSERT INTO midgard.account_type VALUES ('30', 'Credit facility');
INSERT INTO midgard.account_type VALUES ('40', 'Universal account');
INSERT INTO midgard.account_type VALUES ('50', 'Investment account');


/***************************************************************************/
CREATE TABLE midgard.transaction_type (
  id          VARCHAR(2)     NOT NULL,
  type_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT transaction_type_PK PRIMARY KEY (id)
);

INSERT INTO midgard.transaction_type VALUES ('00', 'Goods &amp; service (Purchase)');
INSERT INTO midgard.transaction_type VALUES ('01', 'Cash');
INSERT INTO midgard.transaction_type VALUES ('02', 'Debit adjustment');
INSERT INTO midgard.transaction_type VALUES ('09', 'Purchase with cashback');
INSERT INTO midgard.transaction_type VALUES ('10', 'P2P Debit');
INSERT INTO midgard.transaction_type VALUES ('11', 'Quasi-cash');
INSERT INTO midgard.transaction_type VALUES ('12', 'Manual cash');
INSERT INTO midgard.transaction_type VALUES ('20', 'Returns (refund)');
INSERT INTO midgard.transaction_type VALUES ('21', 'Deposits');
INSERT INTO midgard.transaction_type VALUES ('22', 'Credit adjustment');
INSERT INTO midgard.transaction_type VALUES ('26', 'P2P Credit');
INSERT INTO midgard.transaction_type VALUES ('31', 'Balance inquiry');
INSERT INTO midgard.transaction_type VALUES ('37', 'Mini-statement');
INSERT INTO midgard.transaction_type VALUES ('38', 'Mini-statement ');
INSERT INTO midgard.transaction_type VALUES ('39', 'Mini-statement ');
INSERT INTO midgard.transaction_type VALUES ('50', 'Payment');
INSERT INTO midgard.transaction_type VALUES ('51', 'Payment');
INSERT INTO midgard.transaction_type VALUES ('52', 'Payment');
INSERT INTO midgard.transaction_type VALUES ('70', 'PIN change');
INSERT INTO midgard.transaction_type VALUES ('19', 'Fee Collection (Credit to Transaction Originator)');
INSERT INTO midgard.transaction_type VALUES ('29', 'Fee Collection (Debit to Transaction Originator)');


/***************************************************************************/
CREATE TABLE midgard.card_data_input_capability (
  id                         VARCHAR(1)     NOT NULL,
  card_data_input_cap_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT card_data_input_capability_PK PRIMARY KEY (id)
);

INSERT INTO midgard.card_data_input_capability VALUES ('0', 'Unknown');
INSERT INTO midgard.card_data_input_capability VALUES ('1', 'Manual, no terminal');
INSERT INTO midgard.card_data_input_capability VALUES ('2', 'Magnetic stripe read');
INSERT INTO midgard.card_data_input_capability VALUES ('3', 'Barcode');
INSERT INTO midgard.card_data_input_capability VALUES ('4', 'OCR');
INSERT INTO midgard.card_data_input_capability VALUES ('5', 'ICC');
INSERT INTO midgard.card_data_input_capability VALUES ('6', 'key entered');
INSERT INTO midgard.card_data_input_capability VALUES ('A', 'Contactless magnetic stripe');
INSERT INTO midgard.card_data_input_capability VALUES ('B', 'Magnetic stripe read + key entry');
INSERT INTO midgard.card_data_input_capability VALUES ('C', 'Magnetic stripe read + ICC + key entry');
INSERT INTO midgard.card_data_input_capability VALUES ('D', 'Magnetic stripe read + ICC');
INSERT INTO midgard.card_data_input_capability VALUES ('E', 'ICC+ key entry');
INSERT INTO midgard.card_data_input_capability VALUES ('M', 'Contactless ICC');

/***************************************************************************/
CREATE TABLE midgard.card_data_input_mode (
  id                         VARCHAR(1)     NOT NULL,
  card_data_input_mode_name  VARCHAR(255)   NOT NULL,
  CONSTRAINT card_data_input_mode_PK PRIMARY KEY (id)
);

INSERT INTO midgard.card_data_input_mode VALUES ('0', 'Unspecified');
INSERT INTO midgard.card_data_input_mode VALUES ('1', 'Manual, no terminal');
INSERT INTO midgard.card_data_input_mode VALUES ('2', 'Magnetic stripe read');
INSERT INTO midgard.card_data_input_mode VALUES ('3', 'Bar code');
INSERT INTO midgard.card_data_input_mode VALUES ('4', 'OCR');
INSERT INTO midgard.card_data_input_mode VALUES ('5', 'ICC');
INSERT INTO midgard.card_data_input_mode VALUES ('6', 'Key entered');
INSERT INTO midgard.card_data_input_mode VALUES ('7', 'ICC, CVV may not be checked');
INSERT INTO midgard.card_data_input_mode VALUES ('8', 'ICC fallback to mag.stripe');
INSERT INTO midgard.card_data_input_mode VALUES ('9', 'Full magnetic stripe read');
INSERT INTO midgard.card_data_input_mode VALUES ('A', 'Contactless magnetic-stripe read');
INSERT INTO midgard.card_data_input_mode VALUES ('J', 'Credential On File');
INSERT INTO midgard.card_data_input_mode VALUES ('M', 'Contactless smart card read');
INSERT INTO midgard.card_data_input_mode VALUES ('N', 'Contactless input, PayPass Mapping Service applied');
INSERT INTO midgard.card_data_input_mode VALUES ('R', 'PAN Entry via electronic commerce, including remote chip E - e-commerce');
INSERT INTO midgard.card_data_input_mode VALUES ('W', 'PAN Auto Entry via Server');
INSERT INTO midgard.card_data_input_mode VALUES ('Y', 'PAN Auto Entry via Server, Card on File service applied');


/***************************************************************************/
CREATE TABLE midgard.card_data_output_capability (
  id                         VARCHAR(1)     NOT NULL,
  card_data_output_cap_name  VARCHAR(255)   NOT NULL,
  CONSTRAINT card_data_output_capability_PK PRIMARY KEY (id)
);

INSERT INTO midgard.card_data_output_capability VALUES ('0', 'Unknown; data unavailable');
INSERT INTO midgard.card_data_output_capability VALUES ('1', 'None');
INSERT INTO midgard.card_data_output_capability VALUES ('2', 'Magnetic stripe write');
INSERT INTO midgard.card_data_output_capability VALUES ('3', 'ICC');
INSERT INTO midgard.card_data_output_capability VALUES ('S', 'Other');


/***************************************************************************/
CREATE TABLE midgard.cardholder_auth_capability (
  id                         VARCHAR(1)     NOT NULL,
  cardholder_auth_cap_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT cardholder_auth_capability_PK PRIMARY KEY (id)
);

INSERT INTO midgard.cardholder_auth_capability VALUES ('0', 'no electronic authentication');
INSERT INTO midgard.cardholder_auth_capability VALUES ('1', 'PIN');
INSERT INTO midgard.cardholder_auth_capability VALUES ('2', 'Electronic signature analysis');
INSERT INTO midgard.cardholder_auth_capability VALUES ('3', 'Biometrics');
INSERT INTO midgard.cardholder_auth_capability VALUES ('4', 'Biographic');
INSERT INTO midgard.cardholder_auth_capability VALUES ('5', 'Electronic authentication inoperative');
INSERT INTO midgard.cardholder_auth_capability VALUES ('6', 'Other');


/***************************************************************************/
CREATE TABLE midgard.cardholder_auth_entity (
  id                            VARCHAR(1)     NOT NULL,
  cardholder_auth_entity_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT cardholder_auth_entity_PK PRIMARY KEY (id)
);

INSERT INTO midgard.cardholder_auth_entity VALUES ('0', 'not authenticated');
INSERT INTO midgard.cardholder_auth_entity VALUES ('1', 'ICC');
INSERT INTO midgard.cardholder_auth_entity VALUES ('2', 'CAD');
INSERT INTO midgard.cardholder_auth_entity VALUES ('3', 'Authorizing agent');
INSERT INTO midgard.cardholder_auth_entity VALUES ('4', 'By merchant');
INSERT INTO midgard.cardholder_auth_entity VALUES ('5', 'Other');
INSERT INTO midgard.cardholder_auth_entity VALUES ('9', 'Unknown');


/***************************************************************************/
CREATE TABLE midgard.cardholder_auth_method (
  id                            VARCHAR(1)     NOT NULL,
  cardholder_auth_method_name   VARCHAR(255)   NOT NULL,
  CONSTRAINT cardholder_auth_method_PK PRIMARY KEY (id)
);

INSERT INTO midgard.cardholder_auth_method VALUES ('0', 'Not authenticated');
INSERT INTO midgard.cardholder_auth_method VALUES ('1', 'PIN');
INSERT INTO midgard.cardholder_auth_method VALUES ('2', 'Electronic signature analysis');
INSERT INTO midgard.cardholder_auth_method VALUES ('3', 'Biometrics');
INSERT INTO midgard.cardholder_auth_method VALUES ('4', 'Biographic');
INSERT INTO midgard.cardholder_auth_method VALUES ('5', 'Manual signature verification');
INSERT INTO midgard.cardholder_auth_method VALUES ('6', 'Other manual verification');
INSERT INTO midgard.cardholder_auth_method VALUES ('9', 'Unknown');


/***************************************************************************/
CREATE TABLE midgard.cardholder_presence (
  id                            VARCHAR(1)     NOT NULL,
  cardholder_presence_name      VARCHAR(255)   NOT NULL,
  CONSTRAINT cardholder_presence_PK PRIMARY KEY (id)
);

INSERT INTO midgard.cardholder_presence VALUES ('0', 'Cardholder present');
INSERT INTO midgard.cardholder_presence VALUES ('1', 'Cardholder not present, unspecified');
INSERT INTO midgard.cardholder_presence VALUES ('2', 'Cardholder not present, mail order');
INSERT INTO midgard.cardholder_presence VALUES ('3', 'Cardholder not present, telephone');
INSERT INTO midgard.cardholder_presence VALUES ('4', 'Cardholder not present, standing authorization');
INSERT INTO midgard.cardholder_presence VALUES ('5', 'Cardholder not present, u-commerce');
INSERT INTO midgard.cardholder_presence VALUES ('9', 'Unknown');


/***************************************************************************/
CREATE TABLE midgard.message_function_code (
  code                       INTEGER        NOT NULL,
  message_function_name      VARCHAR(255)   NOT NULL,
  CONSTRAINT message_function_code_PK PRIMARY KEY (code)
);

INSERT INTO midgard.message_function_code VALUES (100, 'Original authorization');
INSERT INTO midgard.message_function_code VALUES (102, 'Replacement authorization');
INSERT INTO midgard.message_function_code VALUES (104, 'Resubmission');
INSERT INTO midgard.message_function_code VALUES (106, 'Supplementary authorization');
INSERT INTO midgard.message_function_code VALUES (108, 'Inquiry');
INSERT INTO midgard.message_function_code VALUES (200, 'Original financial request/advice');
INSERT INTO midgard.message_function_code VALUES (201, 'Previously approved authorization — amount same');
INSERT INTO midgard.message_function_code VALUES (202, 'Previously approved authorization — amount differs');
INSERT INTO midgard.message_function_code VALUES (203, 'Resubmission of a previously denied financial request');
INSERT INTO midgard.message_function_code VALUES (204, 'Resubmission of a previously reversed financial transaction');
INSERT INTO midgard.message_function_code VALUES (205, 'First representment, full');
INSERT INTO midgard.message_function_code VALUES (206, 'Second representment, full');
INSERT INTO midgard.message_function_code VALUES (207, 'Third or subsequent representment, full');
INSERT INTO midgard.message_function_code VALUES (211, 'First representment partial amount');
INSERT INTO midgard.message_function_code VALUES (212, 'Second representment partial amount');
INSERT INTO midgard.message_function_code VALUES (213, 'Third or subsequent representment partial amount');
INSERT INTO midgard.message_function_code VALUES (280, 'First representment reversal, full');
INSERT INTO midgard.message_function_code VALUES (281, 'Second representment reversal, full');
INSERT INTO midgard.message_function_code VALUES (282, 'Third or subsequent representment reversal, full');
INSERT INTO midgard.message_function_code VALUES (283, 'First representment reversal, partial');
INSERT INTO midgard.message_function_code VALUES (284, 'Second representment reversal, partial');
INSERT INTO midgard.message_function_code VALUES (285, 'Third or subsequent representment reversal, partial');
INSERT INTO midgard.message_function_code VALUES (400, 'Full reversal, transaction did not complete as approved');
INSERT INTO midgard.message_function_code VALUES (401, 'Partial reversal, transaction did not complete for full amount');
INSERT INTO midgard.message_function_code VALUES (450, 'First chargeback, full');
INSERT INTO midgard.message_function_code VALUES (451, 'Second chargeback, full');
INSERT INTO midgard.message_function_code VALUES (452, 'Third or subsequent chargeback, full');
INSERT INTO midgard.message_function_code VALUES (453, 'First chargeback, partial');
INSERT INTO midgard.message_function_code VALUES (454, 'Second chargeback, partial');
INSERT INTO midgard.message_function_code VALUES (455, 'Third or subsequent chargeback, partial');
INSERT INTO midgard.message_function_code VALUES (456, 'Final chargeback, full amount');
INSERT INTO midgard.message_function_code VALUES (457, 'Final chargeback, partial amount');
INSERT INTO midgard.message_function_code VALUES (490, 'First chargeback reversal, full');
INSERT INTO midgard.message_function_code VALUES (491, 'Second chargeback reversal, full');
INSERT INTO midgard.message_function_code VALUES (492, 'Third or subsequent chargeback reversal');
INSERT INTO midgard.message_function_code VALUES (493, 'First chargeback reversal, partial');
INSERT INTO midgard.message_function_code VALUES (494, 'Second chargeback reversal, partial');
INSERT INTO midgard.message_function_code VALUES (495, 'Third or subsequent chargeback reversal, partial');
INSERT INTO midgard.message_function_code VALUES (600, 'Retrieval request');
INSERT INTO midgard.message_function_code VALUES (601, 'Original document repeat retrieval');
INSERT INTO midgard.message_function_code VALUES (602, 'Retrieval request response');
INSERT INTO midgard.message_function_code VALUES (613, 'Retrieval not fulfilled');
INSERT INTO midgard.message_function_code VALUES (640, 'Retrieval Request reversal');
INSERT INTO midgard.message_function_code VALUES (700, 'Fee Collection (Member-generated)');
INSERT INTO midgard.message_function_code VALUES (780, 'Fee Collection Return');
INSERT INTO midgard.message_function_code VALUES (781, 'Fee Collection Resubmission');
INSERT INTO midgard.message_function_code VALUES (782, 'Fee Collection Arbitration Return');
INSERT INTO midgard.message_function_code VALUES (783, 'Fee Collection (Clearing System-generated)');
INSERT INTO midgard.message_function_code VALUES (790, 'Fee Collection (Funds Transfer)');
INSERT INTO midgard.message_function_code VALUES (791, 'Fee Collection (Funds Transfer Backout)');


/***************************************************************************/
CREATE TABLE midgard.message_type_id (
  id                         INTEGER        NOT NULL,
  message_type_name          VARCHAR(255)   NOT NULL,
  CONSTRAINT message_type_id_PK PRIMARY KEY (id)
);

INSERT INTO midgard.message_type_id VALUES (1100, 'Online authorization');
INSERT INTO midgard.message_type_id VALUES (1120, 'Online authorization advice');
INSERT INTO midgard.message_type_id VALUES (1200, 'Online financial transaction');
INSERT INTO midgard.message_type_id VALUES (1220, 'Online financial advice ');
INSERT INTO midgard.message_type_id VALUES (1240, 'Presentment');
INSERT INTO midgard.message_type_id VALUES (1440, 'Reversal');
INSERT INTO midgard.message_type_id VALUES (1442, 'Chargeback');
INSERT INTO midgard.message_type_id VALUES (1644, 'Retrieval request');
INSERT INTO midgard.message_type_id VALUES (1740, 'Fee Collection');


/***************************************************************************/
CREATE TABLE midgard.operational_environment (
  id                         VARCHAR(1)     NOT NULL,
  operational_environment    VARCHAR(255)   NOT NULL,
  CONSTRAINT operational_environment_PK PRIMARY KEY (id)
);

INSERT INTO midgard.operational_environment VALUES ('0', 'no terminal used');
INSERT INTO midgard.operational_environment VALUES ('1', 'on premises of card acceptor, attended');
INSERT INTO midgard.operational_environment VALUES ('2', 'on premises of card acceptor, unattended ');
INSERT INTO midgard.operational_environment VALUES ('3', 'off premises of card acceptor, attended');
INSERT INTO midgard.operational_environment VALUES ('4', 'off premises of card acceptor, unattended ');
INSERT INTO midgard.operational_environment VALUES ('5', 'on premises of cardholder, unattended');
INSERT INTO midgard.operational_environment VALUES ('6', 'off premises of cardholder, unattended');
INSERT INTO midgard.operational_environment VALUES ('M', 'Mobile Acceptance solution (mPOS)');
INSERT INTO midgard.operational_environment VALUES ('9', 'Unknown');


/***************************************************************************/
CREATE TABLE midgard.pin_capture_capability (
  id                         VARCHAR(1)     NOT NULL,
  pin_capture_capability     VARCHAR(255)   NOT NULL,
  CONSTRAINT pin_capture_capability_PK PRIMARY KEY (id)
);

INSERT INTO midgard.pin_capture_capability VALUES ('0', 'No PIN capture capability');
INSERT INTO midgard.pin_capture_capability VALUES ('1', 'Unknown; data unavailable');
INSERT INTO midgard.pin_capture_capability VALUES ('4', 'PIN capture capability 4 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('5', 'PIN capture capability 5 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('6', 'PIN capture capability 6 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('7', 'PIN capture capability 7 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('8', 'PIN capture capability 8 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('9', 'PIN capture capability 9 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('A', 'PIN capture capability 10 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('B', 'PIN capture capability 11 characters maximum');
INSERT INTO midgard.pin_capture_capability VALUES ('C', 'PIN capture capability 12 characters maximum');


/***************************************************************************/
CREATE TYPE midgard.merchant_state AS ENUM ('OPEN', 'CLOSE');

CREATE TABLE midgard.merchant (
  merchant_id                  VARCHAR(15)                   NOT NULL,
  merchant_name                VARCHAR(32)                   NOT NULL,
  merchant_address             VARCHAR(64)                   NOT NULL,
  merchant_country             VARCHAR(3)                    NOT NULL,
  merchant_city                VARCHAR(21)                   NOT NULL,
  merchant_postal_code         VARCHAR(10)                   NOT NULL,
  status                       merchant_state                NOT NULL,
  valid_from                   TIMESTAMP WITHOUT TIME ZONE   NOT NULL  DEFAULT (now() at time zone 'utc'),
  valid_to                     TIMESTAMP WITHOUT TIME ZONE   NULL,
  CONSTRAINT merchant_PK PRIMARY KEY (merchant_id)
);

CREATE INDEX merchant_id_idx ON midgard.merchant (merchant_id);


/***************************************************************************/
CREATE TYPE midgard.transaction_clearing_state AS ENUM ('CREATED', 'READY', 'ACTIVE', 'FINISHED', 'FAILED');

CREATE TABLE midgard.clearing_transaction (
  invoice_id                      VARCHAR(100)                  NOT NULL,
  doc_id                          VARCHAR(100)                  NOT NULL,
  transaction_id                  VARCHAR(100)                  NULL,
  transaction_date                TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
  transaction_amount              BIGINT                        NOT NULL,
  transaction_currency            VARCHAR(3)                    NOT NULL,
  transaction_type                VARCHAR(2)                    NOT NULL,
  bank_name                       VARCHAR(255)                  NULL,
  payer_bank_card_token           CHARACTER VARYING             NULL,
  payer_bank_card_payment_system  CHARACTER VARYING             NULL,
  payer_bank_card_bin             CHARACTER VARYING             NULL,
  payer_bank_card_masked_pan      CHARACTER VARYING             NULL,
  payer_bank_card_token_provider  CHARACTER VARYING             NULL,
  transaction_clearing_state      transaction_clearing_state    NOT NULL,
  account_type_from               VARCHAR(2)                    NOT NULL,
  account_type_to                 VARCHAR(2)                    NOT NULL,
  approval_code                   VARCHAR(6)                    NULL,
  card_capture_capability         VARCHAR(1)                    NOT NULL,
  card_data_input_capability      VARCHAR(1)                    NOT NULL,
  card_data_input_mode            VARCHAR(1)                    NOT NULL,
  card_data_output_capability     VARCHAR(1)                    NOT NULL,
  card_presence                   VARCHAR(1)                    NOT NULL,
  cardholder_auth_capability      VARCHAR(1)                    NOT NULL,
  cardholder_auth_entity          VARCHAR(1)                    NOT NULL,
  cardholder_auth_method          VARCHAR(1)                    NOT NULL,
  cardholder_presence             VARCHAR(1)                    NOT NULL,
  e_commerce_security_level       VARCHAR(1)                    NULL,
  mcc                             INTEGER                       NOT NULL,
  merchant_id                     VARCHAR(15)                   NOT NULL,
  message_function_code           INTEGER                       NOT NULL,
  message_reason_code             INTEGER                       NOT NULL,
  message_type_identifier         INTEGER                       NOT NULL,
  operational_environment         VARCHAR(1)                    NOT NULL,
  pin_capture_capability          VARCHAR(1)                    NOT NULL,
  terminal_data_output_capability VARCHAR(1)                    NOT NULL,
  terminal_id                     VARCHAR(8)                    NOT NULL,
  rrn                             VARCHAR(32)                   NULL,
  response_code                   VARCHAR(3)                    NULL,
  system_trace_audit_number       VARCHAR(6)                    NULL,
  clearing_id                     BIGINT                        NULL,
  last_act_time                   TIMESTAMP WITHOUT TIME ZONE   NOT NULL  DEFAULT (now() at time zone 'utc'),

  CONSTRAINT clearing_transaction_PK PRIMARY KEY (invoice_id, doc_id)
);

COMMENT ON COLUMN clearing_transaction.transaction_type IS 'The lookup table: transaction_type';
COMMENT ON COLUMN clearing_transaction.account_type_from IS 'The lookup table: account_type';
COMMENT ON COLUMN clearing_transaction.account_type_to IS 'The lookup table: account_type';
COMMENT ON COLUMN clearing_transaction.card_capture_capability IS 'Possible values for the data element: 0 – None; 1 – Capture; 9 – Unknown';
COMMENT ON COLUMN clearing_transaction.card_capture_capability IS 'The lookup table: card_data_input_capability';
COMMENT ON COLUMN clearing_transaction.card_data_input_mode IS 'The lookup table: card_data_input_mode';
COMMENT ON COLUMN clearing_transaction.card_data_output_capability IS 'The lookup table: card_data_input_capability';
COMMENT ON COLUMN clearing_transaction.card_presence IS 'Possible values for the data element: 0 - card not present, 1 - card present, 9 – Unknown';
COMMENT ON COLUMN clearing_transaction.cardholder_auth_capability IS 'The lookup table: cardholder_auth_capability';
COMMENT ON COLUMN clearing_transaction.cardholder_auth_entity IS 'The lookup table: cardholder_auth_entity';
COMMENT ON COLUMN clearing_transaction.cardholder_auth_method IS 'The lookup table: cardholder_auth_method';
COMMENT ON COLUMN clearing_transaction.cardholder_presence IS 'The lookup table: cardholder_presence';
COMMENT ON COLUMN clearing_transaction.mcc IS 'Merchant category code, also known as card acceptor business code – code classifying the type of business being done by the card acceptor for this transaction';
COMMENT ON COLUMN clearing_transaction.merchant_id IS 'Code identifying the merchant';
COMMENT ON COLUMN clearing_transaction.message_function_code IS 'The lookup table: message_function_code';
COMMENT ON COLUMN clearing_transaction.message_reason_code IS 'The transmitted values must be according to procedure of the processing center used in the transaction - VISA, MC or Local center';
COMMENT ON COLUMN clearing_transaction.message_type_identifier IS 'The lookup table: message_type_id';
COMMENT ON COLUMN clearing_transaction.operational_environment IS 'The lookup table: operational_environment';
COMMENT ON COLUMN clearing_transaction.pin_capture_capability IS 'The lookup table: pin_capture_capability';
COMMENT ON COLUMN clearing_transaction.e_commerce_security_level IS 'The list of possible values: U - Non-secure transaction, V - Channel encryption, S - Merchant secure, cardholder or issuer is not secure, T - Secure e-commerce transaction ';
COMMENT ON COLUMN clearing_transaction.terminal_data_output_capability IS 'The list of possible values: 0 -  Unknown; data unavailable, 1 -  None, 2 -  Printing capability only, 3 -  Display capability only, 4 -  Printing and display capability';


CREATE INDEX clearing_transactions_id_idx ON midgard.clearing_transaction (transaction_id);

CREATE INDEX clearing_transactions_date_idx ON midgard.clearing_transaction (transaction_date, bank_name);


/***************************************************************************/
-- CREATE TABLE midgard.encoding_transaction_status (
--   id                         INTEGER        NOT NULL,
--   encoding_status_name       VARCHAR(100)   NOT NULL,
--   CONSTRAINT encoding_transaction_status_PK PRIMARY KEY (id)
-- );


/***************************************************************************/
-- CREATE TABLE midgard.encoding_transaction_data (
--   clearing_id        BIGINT                        NOT NULL,
--   date               TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
--   transaction_id     VARCHAR(100)                  NOT NULL,
--   transaction_data   CHARACTER VARYING             NOT NULL,
--   state              INTEGER                       NOT NULL,
--   CONSTRAINT encoding_transaction_data_PK PRIMARY KEY (clearing_id, transaction_id)
-- );
--
-- COMMENT ON COLUMN encoding_transaction_data.state IS 'The lookup table: encoding_transaction_status';
--
--
-- CREATE INDEX encoding_transaction_data_idx ON midgard.encoding_transaction_data (clearing_id, transaction_id);
--
-- CREATE INDEX encoding_transaction_data_date_idx ON midgard.encoding_transaction_data (date, clearing_id, transaction_id);


/***************************************************************************/
CREATE TABLE midgard.config (
  name        VARCHAR(150)   NOT NULL,
  value       VARCHAR(150)   NOT NULL,
  comment     VARCHAR(500)   NOT NULL,
  CONSTRAINT config_PK PRIMARY KEY (name)
);

INSERT INTO midgard.config (name, value, comment)
VALUES ('last_event_id', '1', 'ID последнего полученного эвента');

INSERT INTO midgard.config (name, value, comment)
VALUES ('mts.clearing_state', '1', 'Возможность выполнения клиринга для МТС банка');


/***************************************************************************/
CREATE TYPE midgard.clearing_state AS ENUM ('STARTED', 'EXECUTE', 'SUCCESSFULLY', 'FAILED');

CREATE TABLE midgard.clearing_event (
  id           BIGSERIAL                     NOT NULL,
  date         TIMESTAMP WITHOUT TIME ZONE   NOT NULL DEFAULT (now() at time zone 'utc'),
  bank_name    VARCHAR(100)                  NOT NULL,
  state        clearing_state                NOT NULL,
  CONSTRAINT clearing_event_PK PRIMARY KEY (id)
);

CREATE INDEX clearing_event_idx ON midgard.clearing_event (bank_name, date desc, id);


/***************************************************************************/
CREATE TYPE midgard.ct_state AS ENUM ('SENT', 'REFUSED');

CREATE TABLE midgard.clearing_transaction_info (
  clearing_id     BIGINT                        NOT NULL,
  transaction_id  VARCHAR(100)                  NOT NULL,
  ct_state        ct_state                      NOT NULL,
  CONSTRAINT clearing_transaction_info_PK PRIMARY KEY (clearing_id, transaction_id)
);

CREATE INDEX clearing_transaction_info_idx ON midgard.clearing_transaction_info (transaction_id);


/***************************************************************************/
CREATE TABLE midgard.failure_transaction (
  clearing_id            BIGSERIAL                     NOT NULL,
  transaction_id         VARCHAR(100)                  NOT NULL,
  act_time               TIMESTAMP WITHOUT TIME ZONE   NOT NULL DEFAULT (now() at time zone 'utc'),
  reason                 VARCHAR(500)                  NULL,
  CONSTRAINT failure_transaction_PK PRIMARY KEY (clearing_id, transaction_id)
);

CREATE INDEX failure_transaction_idx ON midgard.failure_transaction (transaction_id);


/***************************************************************************/

--//TODO: проверить актуальность тестовых данных

/** TEST MERCHANT DATA */
/*INSERT INTO midgard.merchants(merchant_id, merchant_name, merchant_address,
       merchant_country, merchant_city, merchant_postal_code, status, valid_from, valid_to)
VALUES ('29003001', '231285*EPS', 'address', '643', 'Moscow', '117112',
       transaction_clearing_state.OPEN, now() at time zone 'utc', null);*/



/** TEST TRANSACTION DATA */
/*

INSERT INTO midgard.clearing_transactions( invoice_id, doc_id, transaction_id, transaction_date, transaction_amount,
                                           transaction_currency, transaction_type, bank_name, payer_bank_card_token,
                                           payer_bank_card_payment_system, payer_bank_card_bin, payer_bank_card_masked_pan,
                                           payer_bank_card_token_provider, transaction_clearing_state, account_type_from,
                                           account_type_to, approval_code, card_capture_capability, card_data_input_capability,
                                           card_data_input_mode, card_data_output_capability, card_presence,
                                           cardholder_auth_capability, cardholder_auth_entity, cardholder_auth_method,
                                           cardholder_presence, e_commerce_security_level, mcc, merchant_id,
                                           message_function_code, message_reason_code, message_type_identifier,
                                           operational_environment, pin_capture_capability, terminal_data_output_capability,
                                           terminal_id, rrn, response_code, system_trace_audit_number)
VALUES ('inv_1', 'doc_1', 'tran_1', now(), 100000,
        'RUB', '00', 'mts', null, null, null, null,
        null, transaction_clearing_state.CREATED, '00',
        '00', 'appr_code_none', '0', '0',
        'J', '0', '5',
        '0', '0', '0', '5', null, '5734', '29003001',
        '200', '1508', '1100', null, null, null,
        '29003001', null, null, null)

*/


