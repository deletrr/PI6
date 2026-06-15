-- PontoLivre - Flyway Migration V2
-- Add Vehicles and Session Codes

-- 1. Extend session_status enum (PostgreSQL requires separate transaction or specific syntax for ALTER TYPE)
-- Note: In PostgreSQL, ALTER TYPE ... ADD VALUE cannot be executed in a transaction block in some versions.
-- Flyway handles this by running outside of a transaction if configured, or we can use a workaround.
ALTER TYPE session_status ADD VALUE IF NOT EXISTS 'PENDING';
ALTER TYPE session_status ADD VALUE IF NOT EXISTS 'EXPIRED';

-- 2. Create vehicles table
CREATE TABLE IF NOT EXISTS vehicles (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID             NOT NULL REFERENCES users(id),
    model          VARCHAR(100)     NOT NULL,
    plate          VARCHAR(10)      NOT NULL,
    color          VARCHAR(50)      NOT NULL,
    created_at     TIMESTAMP        NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_vehicles_user_id ON vehicles(user_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_plate ON vehicles(plate);

-- 3. Update parking_sessions table
ALTER TABLE parking_sessions ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE parking_sessions ALTER COLUMN vehicle_plate DROP NOT NULL;
ALTER TABLE parking_sessions ADD COLUMN IF NOT EXISTS vehicle_id UUID REFERENCES vehicles(id);
ALTER TABLE parking_sessions ADD COLUMN IF NOT EXISTS session_code VARCHAR(10);
ALTER TABLE parking_sessions ADD COLUMN IF NOT EXISTS code_expires_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_sessions_session_code ON parking_sessions(session_code);

-- 4. Trigger for vehicles updated_at
DROP TRIGGER IF EXISTS trg_vehicles_updated_at ON vehicles;
CREATE TRIGGER trg_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
