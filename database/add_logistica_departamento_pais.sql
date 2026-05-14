-- Logística: departamento y país de entrega en ventas; departamento en clientes.
-- Ejecutar en producción si usa ddl-auto: validate.

ALTER TABLE ventas ADD COLUMN IF NOT EXISTS departamento_entrega VARCHAR(120);
ALTER TABLE ventas ADD COLUMN IF NOT EXISTS pais_entrega VARCHAR(120);

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS departamento VARCHAR(120);
