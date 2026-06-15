-- PontoLivre - Flyway Migration V1
-- PostgreSQL 15+

-- ENUM TYPES
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE parking_status AS ENUM ('FREE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE session_status AS ENUM ('ACTIVE', 'CLOSED', 'OVERTIME');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE transaction_type AS ENUM ('CREDIT_PIX', 'CREDIT_CARD', 'DEBIT_SESSION', 'DEBIT_FINE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_method AS ENUM ('PIX', 'CREDIT_CARD');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE fine_status AS ENUM ('PENDING', 'PAID', 'DISPUTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(150)        NOT NULL,
    email          VARCHAR(255)        NOT NULL UNIQUE,
    password_hash  VARCHAR(255)        NOT NULL,
    cpf            VARCHAR(14)         NOT NULL UNIQUE,
    phone          VARCHAR(20),
    role           user_role           NOT NULL DEFAULT 'USER',
    balance        NUMERIC(10, 2)      NOT NULL DEFAULT 0.00,
    active         BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_cpf ON users(cpf);

CREATE TABLE IF NOT EXISTS parking_meters (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(20)         NOT NULL UNIQUE,
    description    VARCHAR(200),
    latitude       DOUBLE PRECISION,
    longitude      DOUBLE PRECISION,
    status         parking_status      NOT NULL DEFAULT 'FREE',
    mqtt_topic     VARCHAR(200)        NOT NULL UNIQUE,
    last_seen      TIMESTAMP,
    orphan         BOOLEAN             NOT NULL DEFAULT FALSE,
    active         BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_parking_meters_code ON parking_meters(code);
CREATE INDEX IF NOT EXISTS idx_parking_meters_status ON parking_meters(status);
CREATE INDEX IF NOT EXISTS idx_parking_meters_orphan ON parking_meters(orphan);

CREATE TABLE IF NOT EXISTS parking_sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID             NOT NULL REFERENCES users(id),
    parking_meter_id    UUID             NOT NULL REFERENCES parking_meters(id),
    start_time          TIMESTAMP        NOT NULL DEFAULT NOW(),
    end_time            TIMESTAMP,
    free_until          TIMESTAMP        NOT NULL,
    charged_hours       INTEGER          NOT NULL DEFAULT 0,
    amount_charged      NUMERIC(10, 2)   NOT NULL DEFAULT 0.00,
    status              session_status   NOT NULL DEFAULT 'ACTIVE',
    overtime            BOOLEAN          NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON parking_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_meter_id ON parking_sessions(parking_meter_id);
CREATE INDEX IF NOT EXISTS idx_sessions_status ON parking_sessions(status);
CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON parking_sessions(start_time);

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID                NOT NULL REFERENCES users(id),
    session_id      UUID                REFERENCES parking_sessions(id),
    type            transaction_type    NOT NULL,
    amount          NUMERIC(10, 2)      NOT NULL,
    balance_before  NUMERIC(10, 2)      NOT NULL,
    balance_after   NUMERIC(10, 2)      NOT NULL,
    description     VARCHAR(500)        NOT NULL,
    payment_method  payment_method,
    reference_code  VARCHAR(100),
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON wallet_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_session_id ON wallet_transactions(session_id);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON wallet_transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON wallet_transactions(created_at);

CREATE TABLE IF NOT EXISTS fines (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    session_id      UUID            NOT NULL REFERENCES parking_sessions(id),
    amount          NUMERIC(10, 2)  NOT NULL,
    reason          VARCHAR(500)    NOT NULL,
    status          fine_status     NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fines_user_id ON fines(user_id);
CREATE INDEX IF NOT EXISTS idx_fines_session_id ON fines(session_id);
CREATE INDEX IF NOT EXISTS idx_fines_status ON fines(status);

CREATE TABLE IF NOT EXISTS mqtt_logs (
    id              BIGSERIAL PRIMARY KEY,
    topic           VARCHAR(200)    NOT NULL,
    payload         VARCHAR(100)    NOT NULL,
    meter_code      VARCHAR(20),
    processed       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mqtt_logs_topic ON mqtt_logs(topic);
CREATE INDEX IF NOT EXISTS idx_mqtt_logs_created_at ON mqtt_logs(created_at);

CREATE TABLE IF NOT EXISTS support_tickets (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id),
    subject     VARCHAR(200)    NOT NULL,
    message     TEXT            NOT NULL,
    response    TEXT,
    resolved    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_support_user_id ON support_tickets(user_id);

-- Seed data
INSERT INTO users (name, email, password_hash, cpf, phone, role, balance)
SELECT 'Administrador','admin@pontolivre.com',
       '$2a$10$7QJ1kqk7JThghE8vEPy5XuDrV5X.iS8MV8fKW0bNJBPJPOL6tPeZi',
       '000.000.000-00','(11) 99999-0000','ADMIN',0.00
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='admin@pontolivre.com');

INSERT INTO users (name, email, password_hash, cpf, phone, role, balance)
SELECT 'João Silva','joao@email.com',
       '$2a$10$N9LRq3s8X1kP2ZdT4uVmHuEqG5b3nJlI7vY2fD0wO8mC6hA4pRtKa',
       '123.456.789-09','(11) 98765-4321','USER',50.00
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email='joao@email.com');
