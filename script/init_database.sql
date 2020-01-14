-- Setting timezone
SET TIME ZONE 'UTC';

-- allow low-level commands on a remote DB
CREATE EXTENSION IF NOT EXISTS dblink;

-- ensure required user has been created
DO
$body$
BEGIN
   IF NOT EXISTS (
      SELECT *
      FROM   pg_catalog.pg_user
      WHERE  usename = 'nukr_user') THEN

      CREATE ROLE nukr_user LOGIN PASSWORD 'pkasndaosdnsísdoums';
   END IF;
END
$body$;

-- ensure required databases have been created
DO
$doDev$
BEGIN

IF EXISTS (SELECT 1 FROM pg_database WHERE datname = 'nukr') THEN
   RAISE NOTICE 'Database nukr already exists';
ELSE
   PERFORM dblink_exec('dbname=' || current_database()  -- current db
                     , 'CREATE DATABASE nukr OWNER nukr_user');
END IF;

END
$doDev$;


DO
$doTest$
BEGIN

IF EXISTS (SELECT 1 FROM pg_database WHERE datname = 'nukr_test') THEN
   RAISE NOTICE 'Database nukr_test already exists';
ELSE
   PERFORM dblink_exec('dbname=' || current_database()  -- current db
                     , 'CREATE DATABASE nukr_test OWNER nukr_user');
END IF;

END
$doTest$;

GRANT ALL PRIVILEGES ON DATABASE nukr to nukr_user;
GRANT ALL PRIVILEGES ON DATABASE nukr_test to nukr_user;

-- add case-insensitive option to both databases
\c nukr;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c nukr_test;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
