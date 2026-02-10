-- Schema management is handled by Hibernate (ddl-auto: update)
-- This file is kept for reference only.
-- spring.sql.init.mode is set to 'never' in application.yaml

-- CREATE TABLE IF NOT EXISTS public.roles (
--   id BIGSERIAL PRIMARY KEY,
--   nombre VARCHAR(50) NOT NULL UNIQUE
-- );

-- CREATE TABLE IF NOT EXISTS public.usuarios (
--   id BIGSERIAL PRIMARY KEY,
--   username VARCHAR(50) UNIQUE NOT NULL,
--   password VARCHAR(255) NOT NULL,
--   activo BOOLEAN,
--   rol_id BIGINT REFERENCES roles(id)
-- );
