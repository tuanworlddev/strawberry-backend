-- Register V9-V13 in flyway_schema_history so Flyway considers them already applied
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
  (9,  '9',  'add scheduler and metrics fields',  'SQL', 'V9__add_scheduler_and_metrics_fields.sql',   -1, 'strawberry_user', 100, true),
  (10, '10', 'create carts',                       'SQL', 'V10__create_carts.sql',                      -1, 'strawberry_user', 100, true),
  (11, '11', 'create orders',                      'SQL', 'V11__create_orders.sql',                     -1, 'strawberry_user', 100, true),
  (12, '12', 'create payment confirmations',       'SQL', 'V12__create_payment_confirmations.sql',      -1, 'strawberry_user', 100, true),
  (13, '13', 'add reserved stock',                 'SQL', 'V13__add_reserved_stock.sql',                -1, 'strawberry_user', 100, true)
ON CONFLICT DO NOTHING;

SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
