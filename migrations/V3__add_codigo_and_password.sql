-- Migration: V3__add_codigo_and_password.sql
-- Add codigo and password fields to users table

-- Add codigo column
ALTER TABLE users ADD COLUMN IF NOT EXISTS codigo VARCHAR(50) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL;

-- Create index on codigo
CREATE INDEX IF NOT EXISTS idx_users_codigo ON users(codigo);

-- Insert sample users (password is: password123 encoded with BCrypt)
-- Generated with BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (codigo, email, nombre, password, rol, activo) VALUES
('20230001', 'juan.perez@utp.edu.pe', 'Juan Perez', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ESTUDIANTE', TRUE),
('20230002', 'maria.garcia@utp.edu.pe', 'Maria Garcia', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ESTUDIANTE', TRUE),
('CAJERO01', 'carlos.ruiz@utp.edu.pe', 'Carlos Ruiz', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMINISTRATIVO', TRUE),
('ADMIN01', 'admin.utp@utp.edu.pe', 'Administrador', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMINISTRATIVO', TRUE)
ON CONFLICT DO NOTHING;