-- PontoLivre - Flyway Migration V3
-- Add CREDIT_REFUND to transaction_type enum

ALTER TYPE transaction_type ADD VALUE IF NOT EXISTS 'CREDIT_REFUND';
