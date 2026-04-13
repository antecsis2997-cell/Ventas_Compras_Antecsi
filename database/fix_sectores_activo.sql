-- Idempotente: deja `sectores.activo` como NOT NULL con default true (PostgreSQL).
-- Ejecutar si al arrancar la app falló el DDL de `activo` o quedaron NULLs.

ALTER TABLE public.sectores ADD COLUMN IF NOT EXISTS activo boolean DEFAULT true;

UPDATE public.sectores SET activo = true WHERE activo IS NULL;

ALTER TABLE public.sectores ALTER COLUMN activo SET DEFAULT true;
ALTER TABLE public.sectores ALTER COLUMN activo SET NOT NULL;
