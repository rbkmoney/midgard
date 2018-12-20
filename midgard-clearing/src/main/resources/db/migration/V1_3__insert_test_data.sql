
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