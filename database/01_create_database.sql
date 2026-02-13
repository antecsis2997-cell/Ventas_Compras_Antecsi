-- ============================================
-- VENTAS_COMPRAS_ANTECSI - Creación de Base de Datos
-- PostgreSQL
-- ============================================

-- Crear base de datos (ejecutar como superusuario/postgres)
-- CREATE DATABASE ventas_db
--   WITH OWNER = ventas_user
--   ENCODING = 'UTF8'
--   LC_COLLATE = 'es_ES.UTF-8'
--   LC_CTYPE = 'es_ES.UTF-8'
--   TEMPLATE = template0;

-- Crear usuario si no existe (ejecutar como superusuario)
-- CREATE USER ventas_user WITH PASSWORD 'PasswordSeguro123';
-- GRANT ALL PRIVILEGES ON DATABASE ventas_db TO ventas_user;
-- \c ventas_db
-- GRANT ALL ON SCHEMA public TO ventas_user;
-- ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO ventas_user;
