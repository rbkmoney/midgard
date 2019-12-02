DROP SCHEMA IF EXISTS feed CASCADE;

CREATE UNIQUE INDEX unique_clearing_event_idx ON midgard.clearing_event_info (event_id, id);
