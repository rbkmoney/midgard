create or replace function feed.cashflow_sum_finalfunc(amount bigint)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return amount;
end;
$$;


create or replace function feed.get_cashflow_sum(_cash_flow feed.cash_flow, obj_type feed.payment_change_type,
                         source_account_type feed.cash_flow_account, source_account_type_values varchar[],
                         destination_account_type feed.cash_flow_account, destination_account_type_values varchar[])
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return (
    coalesce(
      (
        select amount from (select ($1).*) as cash_flow
          where cash_flow.obj_type = $2
          and cash_flow.source_account_type = $3
          and cash_flow.source_account_type_value = ANY ($4)
          and cash_flow.destination_account_type = $5
          and cash_flow.destination_account_type_value = ANY ($6)
          and (
            (cash_flow.obj_type = 'adjustment' and cash_flow.adj_flow_type = 'new_cash_flow')
            or (cash_flow.obj_type != 'adjustment' and cash_flow.adj_flow_type is null)
          )
        ), 0)
  );
end;
$$;


/******************************************************************************************************************/


create or replace function feed.get_payment_amount_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payment'::feed.payment_change_type,
      'provider'::feed.cash_flow_account,
      '{"settlement"}',
      'merchant'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payment_amount(feed.cash_flow)
(
  sfunc     = feed.get_payment_amount_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payment_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payment'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'system'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payment_fee(feed.cash_flow)
(
  sfunc     = feed.get_payment_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payment_external_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payment'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'external'::feed.cash_flow_account,
      '{"income", "outcome"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payment_external_fee(feed.cash_flow)
(
  sfunc     = feed.get_payment_external_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payment_provider_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payment'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'provider'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payment_provider_fee(feed.cash_flow)
(
  sfunc     = feed.get_payment_provider_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payment_guarantee_deposit_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payment'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'merchant'::feed.cash_flow_account,
      '{"guarantee"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payment_guarantee_deposit(feed.cash_flow)
(
  sfunc     = feed.get_payment_guarantee_deposit_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_refund_amount_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'refund'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'provider'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_refund_amount(feed.cash_flow)
(
  sfunc     = feed.get_refund_amount_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_refund_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'refund'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'system'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_refund_fee(feed.cash_flow)
(
  sfunc     = feed.get_refund_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_refund_external_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'refund'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'external'::feed.cash_flow_account,
      '{"income", "outcome"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_refund_external_fee(feed.cash_flow)
(
  sfunc     = feed.get_refund_external_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_refund_provider_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'refund'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'provider'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_refund_provider_fee(feed.cash_flow)
(
  sfunc     = feed.get_refund_provider_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payout_amount_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payout'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'merchant'::feed.cash_flow_account,
      '{"payout"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payout_amount(feed.cash_flow)
(
  sfunc     = feed.get_payout_amount_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_payout_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payout'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'system'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payout_fee(feed.cash_flow)
(
  sfunc     = feed.get_payout_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);


create or replace function feed.get_payout_fixed_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'payout'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"payout"}',
      'system'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_payout_fixed_fee(feed.cash_flow)
(
  sfunc     = feed.get_payout_fixed_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_adjustment_amount_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'adjustment'::feed.payment_change_type,
      'provider'::feed.cash_flow_account,
      '{"settlement"}',
      'merchant'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_adjustment_amount(feed.cash_flow)
(
  sfunc     = feed.get_adjustment_amount_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_adjustment_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'adjustment'::feed.payment_change_type,
      'merchant'::feed.cash_flow_account,
      '{"settlement"}',
      'system'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_adjustment_fee(feed.cash_flow)
(
  sfunc     = feed.get_adjustment_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_adjustment_external_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'adjustment'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'external'::feed.cash_flow_account,
      '{"income", "outcome"}'
    )
  );
end;
$$;


/******************************************************************************************************************/


create aggregate feed.get_adjustment_external_fee(feed.cash_flow)
(
  sfunc     = feed.get_adjustment_external_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);

create or replace function feed.get_adjustment_provider_fee_sfunc(amount bigint, cash_flow feed.cash_flow)
returns bigint
language plpgsql
immutable
parallel safe
as $$
begin
  return $1 + (
    feed.get_cashflow_sum(
      $2,
      'adjustment'::feed.payment_change_type,
      'system'::feed.cash_flow_account,
      '{"settlement"}',
      'provider'::feed.cash_flow_account,
      '{"settlement"}'
    )
  );
end;
$$;

create aggregate feed.get_adjustment_provider_fee(feed.cash_flow)
(
  sfunc     = feed.get_adjustment_provider_fee_sfunc,
  stype     = bigint,
  finalfunc = feed.cashflow_sum_finalfunc,
  parallel  = safe,
  initcond  = 0
);