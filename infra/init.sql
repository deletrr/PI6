-- ============================================================
-- PONTOLIVRE - DATABASE SCHEMA
-- PostgreSQL 15+
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE parking_status AS ENUM ('FREE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE');
CREATE TYPE session_status AS ENUM ('ACTIVE', 'CLOSED', 'OVERTIME', 'PENDING', 'EXPIRED');
CREATE TYPE transaction_type AS ENUM ('CREDIT_PIX', 'CREDIT_CARD', 'DEBIT_SESSION', 'DEBIT_FINE', 'CREDIT_REFUND');
CREATE TYPE payment_method AS ENUM ('PIX', 'CREDIT_CARD');
CREATE TYPE fine_status AS ENUM ('PENDING', 'PAID', 'DISPUTED');

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cpf ON users(cpf);

-- ============================================================
-- VEHICLES
-- ============================================================

CREATE TABLE vehicles (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id        UUID             NOT NULL REFERENCES users(id),
    model          VARCHAR(100)     NOT NULL,
    plate          VARCHAR(10)      NOT NULL,
    color          VARCHAR(50)      NOT NULL,
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vehicles_user_id ON vehicles(user_id);
CREATE INDEX idx_vehicles_plate ON vehicles(plate);

-- ============================================================
-- PARKING METERS (parquímetros)
-- ============================================================

CREATE TABLE parking_meters (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

CREATE INDEX idx_parking_meters_code ON parking_meters(code);
CREATE INDEX idx_parking_meters_status ON parking_meters(status);
CREATE INDEX idx_parking_meters_orphan ON parking_meters(orphan);

-- ============================================================
-- PARKING SESSIONS (tickets)
-- ============================================================

CREATE TABLE parking_sessions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id             UUID             REFERENCES users(id),
    parking_meter_id    UUID             NOT NULL REFERENCES parking_meters(id),
    vehicle_id          UUID             REFERENCES vehicles(id),
    vehicle_plate       VARCHAR(10),
    session_code        VARCHAR(10),
    code_expires_at     TIMESTAMP,
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

CREATE INDEX idx_sessions_user_id ON parking_sessions(user_id);
CREATE INDEX idx_sessions_meter_id ON parking_sessions(parking_meter_id);
CREATE INDEX idx_sessions_session_code ON parking_sessions(session_code);
CREATE INDEX idx_sessions_status ON parking_sessions(status);
CREATE INDEX idx_sessions_start_time ON parking_sessions(start_time);

-- ============================================================
-- WALLET TRANSACTIONS (extratos)
-- ============================================================

CREATE TABLE wallet_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

CREATE INDEX idx_transactions_user_id ON wallet_transactions(user_id);
CREATE INDEX idx_transactions_session_id ON wallet_transactions(session_id);
CREATE INDEX idx_transactions_type ON wallet_transactions(type);
CREATE INDEX idx_transactions_created_at ON wallet_transactions(created_at);

-- ============================================================
-- FINES (multas / infrações)
-- ============================================================

CREATE TABLE fines (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    session_id      UUID            NOT NULL REFERENCES parking_sessions(id),
    amount          NUMERIC(10, 2)  NOT NULL,
    reason          VARCHAR(500)    NOT NULL,
    status          fine_status     NOT NULL DEFAULT 'PENDING',
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fines_user_id ON fines(user_id);
CREATE INDEX idx_fines_session_id ON fines(session_id);
CREATE INDEX idx_fines_status ON fines(status);

-- ============================================================
-- MQTT LOGS
-- ============================================================

CREATE TABLE mqtt_logs (
    id              BIGSERIAL PRIMARY KEY,
    topic           VARCHAR(200)    NOT NULL,
    payload         VARCHAR(100)    NOT NULL,
    meter_code      VARCHAR(20),
    processed       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mqtt_logs_topic ON mqtt_logs(topic);
CREATE INDEX idx_mqtt_logs_created_at ON mqtt_logs(created_at);

-- ============================================================
-- SUPPORT TICKETS
-- ============================================================

CREATE TABLE support_tickets (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID            NOT NULL REFERENCES users(id),
    subject     VARCHAR(200)    NOT NULL,
    message     TEXT            NOT NULL,
    response    TEXT,
    resolved    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_support_user_id ON support_tickets(user_id);

-- ============================================================
-- TRIGGER: updated_at auto-update
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_meters_updated_at
    BEFORE UPDATE ON parking_meters
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_sessions_updated_at
    BEFORE UPDATE ON parking_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_fines_updated_at
    BEFORE UPDATE ON fines
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_support_updated_at
    BEFORE UPDATE ON support_tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================
-- SEED DATA
-- ============================================================

-- Admin user (senha: Admin@123)
-- Usamos a função crypt para gerar o hash BCrypt nativamente,
-- evitando corrupção de caracteres especiais no hash.
INSERT INTO users (name, email, password_hash, cpf, phone, role, balance)
VALUES (
    'Administrador',
    'admin@pontolivre.com',
    crypt('Admin@123', gen_salt('bf')),
    '000.000.000-00',
    '(11) 99999-0000',
    'ADMIN',
    0.00
);

-- Demo user (senha: User@123)
INSERT INTO users (name, email, password_hash, cpf, phone, role, balance)
VALUES (
    'João Silva',
    'joao@email.com',
    crypt('User@123', gen_salt('bf')),
    '123.456.789-09',
    '(11) 98765-4321',
    'USER',
    50.00
);

-- Demo parking meters
INSERT INTO parking_meters (code, description, latitude, longitude, mqtt_topic, orphan)
VALUES
    ('PKM-001', 'Vaga 01 - Rua das Flores', -23.5505, -46.6333, 'parquimetro/PKM-001/status', FALSE),
    ('PKM-002', 'Vaga 02 - Rua das Flores', -23.5510, -46.6340, 'parquimetro/PKM-002/status', FALSE),
    ('PKM-003', 'Vaga 03 - Av. Paulista', -23.5620, -46.6560, 'parquimetro/PKM-003/status', FALSE),
    ('PKM-004', 'Vaga 04 - Av. Paulista', -23.5625, -46.6565, 'parquimetro/PKM-004/status', FALSE),
    ('PKM-005', 'Vaga Órfã - Sem Localização', NULL, NULL, 'parquimetro/PKM-005/status', TRUE);
