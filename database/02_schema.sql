-- ============================================
-- VENTAS_COMPRAS_ANTECSI - Estructura de Tablas
-- PostgreSQL - Schema Public
-- ============================================

-- Orden de creación: tablas sin dependencias primero, luego las que tienen FKs

-- 1. ROLES
CREATE TABLE IF NOT EXISTS public.roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- 2. USUARIOS
CREATE TABLE IF NOT EXISTS public.usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    correo VARCHAR(255),
    rol_id BIGINT REFERENCES public.roles(id),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON public.usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON public.usuarios(username);

-- 3. CATEGORIAS
CREATE TABLE IF NOT EXISTS public.categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

-- 4. PRODUCTOS
CREATE TABLE IF NOT EXISTS public.productos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(255) UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(12, 2) NOT NULL,
    precio_compra DECIMAL(12, 2),
    stock INTEGER NOT NULL,
    categoria_id BIGINT REFERENCES public.categorias(id),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON public.productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON public.productos(codigo);

-- 5. CLIENTES
CREATE TABLE IF NOT EXISTS public.clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(50) NOT NULL,
    tipo_documento VARCHAR(50),
    documento VARCHAR(50) UNIQUE,
    direccion VARCHAR(500),
    activo BOOLEAN DEFAULT true
);

-- 6. PROVEEDORES
CREATE TABLE IF NOT EXISTS public.proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    ruc VARCHAR(50) UNIQUE,
    email VARCHAR(255),
    telefono VARCHAR(100),
    direccion VARCHAR(500),
    activo BOOLEAN DEFAULT true
);

-- 7. MÉTODOS DE PAGO
CREATE TABLE IF NOT EXISTS public.metodos_pago (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    activo BOOLEAN DEFAULT true
);

-- 8. VENTAS
CREATE TABLE IF NOT EXISTS public.ventas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES public.clientes(id),
    usuario_id BIGINT NOT NULL REFERENCES public.usuarios(id),
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id),
    fecha TIMESTAMP,
    total DECIMAL(12, 2),
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    observaciones VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_ventas_cliente ON public.ventas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON public.ventas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON public.ventas(fecha);

-- 9. VENTA DETALLE
CREATE TABLE IF NOT EXISTS public.venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    cantidad INTEGER,
    precio_unitario DECIMAL(12, 2)
);

CREATE INDEX IF NOT EXISTS idx_venta_detalle_venta ON public.venta_detalle(venta_id);
CREATE INDEX IF NOT EXISTS idx_venta_detalle_producto ON public.venta_detalle(producto_id);

-- 10. COMPRAS
CREATE TABLE IF NOT EXISTS public.compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES public.proveedores(id),
    usuario_id BIGINT NOT NULL REFERENCES public.usuarios(id),
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id),
    fecha TIMESTAMP,
    total DECIMAL(12, 2),
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    observaciones VARCHAR(500),
    numero_documento VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_compras_proveedor ON public.compras(proveedor_id);
CREATE INDEX IF NOT EXISTS idx_compras_usuario ON public.compras(usuario_id);
CREATE INDEX IF NOT EXISTS idx_compras_fecha ON public.compras(fecha);

-- 11. COMPRA DETALLE
CREATE TABLE IF NOT EXISTS public.compra_detalle (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES public.compras(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    cantidad INTEGER,
    precio_unitario DECIMAL(12, 2)
);

CREATE INDEX IF NOT EXISTS idx_compra_detalle_compra ON public.compra_detalle(compra_id);
CREATE INDEX IF NOT EXISTS idx_compra_detalle_producto ON public.compra_detalle(producto_id);
