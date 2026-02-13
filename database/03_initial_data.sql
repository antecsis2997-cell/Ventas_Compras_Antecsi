-- ============================================
-- VENTAS_COMPRAS_ANTECSI - Datos Iniciales
-- Roles, Métodos de Pago
-- El usuario admin se crea via DataInitializer.java (con password encriptado)
-- ============================================

-- ROLES (SUPERUSUARIO = dueño del software, único que puede crear admins y cajeros)
INSERT INTO public.roles (nombre) VALUES ('SUPERUSUARIO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.roles (nombre) VALUES ('ADMIN') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.roles (nombre) VALUES ('CAJERO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.roles (nombre) VALUES ('ALMACENERO') ON CONFLICT (nombre) DO NOTHING;

-- MÉTODOS DE PAGO
INSERT INTO public.metodos_pago (nombre, activo) VALUES ('EFECTIVO', true) ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.metodos_pago (nombre, activo) VALUES ('TARJETA', true) ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.metodos_pago (nombre, activo) VALUES ('TRANSFERENCIA', true) ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.metodos_pago (nombre, activo) VALUES ('YAPE', true) ON CONFLICT (nombre) DO NOTHING;
INSERT INTO public.metodos_pago (nombre, activo) VALUES ('PLIN', true) ON CONFLICT (nombre) DO NOTHING;
