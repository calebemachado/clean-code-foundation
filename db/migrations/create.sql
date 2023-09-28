DO
$$
BEGIN
    IF
EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = 'cccat13') THEN
        -- Drop the schema if it exists
        EXECUTE 'DROP SCHEMA cccat13 CASCADE';
        RAISE
NOTICE 'Schema cccat13 dropped.';
END IF;

    -- Create the schema
EXECUTE 'CREATE SCHEMA cccat13';
RAISE
NOTICE 'Schema cccat13 created.';
END $$;


create table cccat13.account
(
  account_id        uuid,
  name              text,
  email             text,
  cpf               text,
  car_plate         text,
  is_passenger      boolean,
  is_driver         boolean,
  date              timestamp,
  is_verified       boolean,
  verification_code uuid
);
