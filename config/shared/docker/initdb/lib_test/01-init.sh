#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Create lib_test database and user for integration tests
# ═══════════════════════════════════════════════════════════════════
set -e
echo "[init] Creating lib_test database and user for tests..."
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create lib_test user if not exists
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'lib_test') THEN
            CREATE USER lib_test WITH PASSWORD 'lib_test';
        END IF;
    END
    \$\$;
    -- Create lib_test database if not exists
    SELECT 'CREATE DATABASE lib_test'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'lib_test')\gexec
    -- Grant privileges
    GRANT ALL PRIVILEGES ON DATABASE lib_test TO lib_test;
EOSQL
# Connect to lib_test database and grant schema privileges
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "lib_test" <<-EOSQL
    GRANT ALL ON SCHEMA public TO lib_test;
EOSQL
echo "[init] ✓ lib_test database and user created successfully"
