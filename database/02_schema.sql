-- ============================================
-- VENTAS_COMPRAS_ANTECSI - Estructura de Tablas
-- PostgreSQL - Schema Public
-- Multi-tenant: cada bodega (sector) tiene sus datos aislados
-- ============================================

-- Orden de creación: tablas sin dependencias primero, luego las que tienen FKs

-- 1. ROLES (global, compartido entre todos los tenants)
CREATE TABLE IF NOT EXISTS public.roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- 2. SECTORES (Bodegas/Tenants - cada bodega es un cliente independiente)
CREATE TABLE IF NOT EXISTS public.sectores (
    id BIGSERIAL PRIMARY KEY,
    nombre_sector VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(200)
);

-- 3. USUARIOS (pertenecen a un sector/bodega)
CREATE TABLE IF NOT EXISTS public.usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    correo VARCHAR(255),
    fecha_nacimiento DATE,
    sede_id BIGINT REFERENCES public.sectores(id),
    usuario_principal_id BIGINT,
    rol_id BIGINT REFERENCES public.roles(id),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON public.usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON public.usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_sede ON public.usuarios(sede_id);

-- 4. CATEGORIAS (por bodega)
CREATE TABLE IF NOT EXISTS public.categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    sector_id BIGINT REFERENCES public.sectores(id)
);

CREATE INDEX IF NOT EXISTS idx_categorias_sector ON public.categorias(sector_id);

-- 5. PRODUCTOS (por bodega - cada bodega tiene su propio catálogo)
CREATE TABLE IF NOT EXISTS public.productos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(255),
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(12, 2) NOT NULL,
    precio_compra DECIMAL(12, 2),
    stock INTEGER NOT NULL,
    categoria_id BIGINT REFERENCES public.categorias(id),
    sector_id BIGINT REFERENCES public.sectores(id),
    unidad_medida VARCHAR(20),
    imagen_url VARCHAR(500),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    stock_minimo_alerta INTEGER,
    tipo VARCHAR(100),
    marca VARCHAR(100),
    cantidad DECIMAL(12, 2),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_productos_categoria ON public.productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_sector ON public.productos(sector_id);
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON public.productos(codigo);

-- Migración: agregar columna moneda si no existe
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='productos' AND column_name='moneda') THEN
    ALTER TABLE public.productos ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'PEN';
  END IF;
END $$;

-- Migración: agregar columnas tipo, marca, cantidad si no existen
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='productos' AND column_name='tipo') THEN
    ALTER TABLE public.productos ADD COLUMN tipo VARCHAR(100);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='productos' AND column_name='marca') THEN
    ALTER TABLE public.productos ADD COLUMN marca VARCHAR(100);
  END IF;
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='productos' AND column_name='cantidad') THEN
    ALTER TABLE public.productos ADD COLUMN cantidad DECIMAL(12, 2);
  END IF;
END $$;

-- 6. CLIENTES (por bodega - cada bodega tiene su propia cartera)
CREATE TABLE IF NOT EXISTS public.clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(50) NOT NULL,
    tipo_documento VARCHAR(50),
    documento VARCHAR(50),
    direccion VARCHAR(500),
    sector_id BIGINT REFERENCES public.sectores(id),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_clientes_sector ON public.clientes(sector_id);

-- 7. PROVEEDORES (por bodega)
CREATE TABLE IF NOT EXISTS public.proveedores (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    ruc VARCHAR(50),
    email VARCHAR(255),
    telefono VARCHAR(100),
    direccion VARCHAR(500),
    sector_id BIGINT REFERENCES public.sectores(id),
    activo BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_proveedores_sector ON public.proveedores(sector_id);

-- 8. MÉTODOS DE PAGO (global, compartido)
CREATE TABLE IF NOT EXISTS public.metodos_pago (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    activo BOOLEAN DEFAULT true
);

-- 9. VENTAS (por bodega)
CREATE TABLE IF NOT EXISTS public.ventas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES public.clientes(id),
    usuario_id BIGINT NOT NULL REFERENCES public.usuarios(id),
    sector_id BIGINT REFERENCES public.sectores(id),
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id),
    fecha TIMESTAMP,
    total DECIMAL(12, 2),
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    tipo_documento VARCHAR(50),
    numero_documento VARCHAR(50),
    observaciones VARCHAR(500),
    moneda VARCHAR(3) NOT NULL DEFAULT 'PEN',
    con_cuotas BOOLEAN
);

-- Migración: agregar columna moneda a ventas si no existe
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='moneda') THEN
    ALTER TABLE public.ventas ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'PEN';
  END IF;
END $$;

-- Migración: agregar columna con_cuotas si no existe
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='con_cuotas') THEN
    ALTER TABLE public.ventas ADD COLUMN con_cuotas BOOLEAN;
  END IF;
END $$;

-- Migración: agregar columnas de delivery
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='requiere_delivery') THEN
    ALTER TABLE public.ventas ADD COLUMN requiere_delivery BOOLEAN DEFAULT false;
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='tipo_entrega') THEN
    ALTER TABLE public.ventas ADD COLUMN tipo_entrega VARCHAR(20);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='direccion_entrega') THEN
    ALTER TABLE public.ventas ADD COLUMN direccion_entrega VARCHAR(500);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='estado_entrega') THEN
    ALTER TABLE public.ventas ADD COLUMN estado_entrega VARCHAR(20);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='usuario_entrega_id') THEN
    ALTER TABLE public.ventas ADD COLUMN usuario_entrega_id BIGINT REFERENCES public.usuarios(id);
  END IF;
END $$;
-- Tracking y confirmación
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='codigo_tracking') THEN
    ALTER TABLE public.ventas ADD COLUMN codigo_tracking VARCHAR(50);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='confirmacion_firma') THEN
    ALTER TABLE public.ventas ADD COLUMN confirmacion_firma TEXT;
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='confirmacion_correo') THEN
    ALTER TABLE public.ventas ADD COLUMN confirmacion_correo VARCHAR(255);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='confirmacion_telefono') THEN
    ALTER TABLE public.ventas ADD COLUMN confirmacion_telefono VARCHAR(50);
  END IF;
END $$;
DO $$ BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='confirmacion_fecha') THEN
    ALTER TABLE public.ventas ADD COLUMN confirmacion_fecha TIMESTAMP;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_ventas_cliente ON public.ventas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_ventas_usuario ON public.ventas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ventas_sector ON public.ventas(sector_id);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON public.ventas(fecha);

-- 10. VENTA DETALLE
CREATE TABLE IF NOT EXISTS public.venta_detalle (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    cantidad INTEGER,
    precio_unitario DECIMAL(12, 2)
);

CREATE INDEX IF NOT EXISTS idx_venta_detalle_venta ON public.venta_detalle(venta_id);
CREATE INDEX IF NOT EXISTS idx_venta_detalle_producto ON public.venta_detalle(producto_id);

-- 11. COMPRAS (por bodega)
CREATE TABLE IF NOT EXISTS public.compras (
    id BIGSERIAL PRIMARY KEY,
    proveedor_id BIGINT NOT NULL REFERENCES public.proveedores(id),
    usuario_id BIGINT NOT NULL REFERENCES public.usuarios(id),
    sector_id BIGINT REFERENCES public.sectores(id),
    metodo_pago_id BIGINT REFERENCES public.metodos_pago(id),
    fecha TIMESTAMP,
    total DECIMAL(12, 2),
    estado VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    observaciones VARCHAR(500),
    numero_documento VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_compras_proveedor ON public.compras(proveedor_id);
CREATE INDEX IF NOT EXISTS idx_compras_usuario ON public.compras(usuario_id);
CREATE INDEX IF NOT EXISTS idx_compras_sector ON public.compras(sector_id);
CREATE INDEX IF NOT EXISTS idx_compras_fecha ON public.compras(fecha);

-- 12. COMPRA DETALLE
CREATE TABLE IF NOT EXISTS public.compra_detalle (
    id BIGSERIAL PRIMARY KEY,
    compra_id BIGINT NOT NULL REFERENCES public.compras(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    cantidad INTEGER,
    precio_unitario DECIMAL(12, 2)
);

CREATE INDEX IF NOT EXISTS idx_compra_detalle_compra ON public.compra_detalle(compra_id);
CREATE INDEX IF NOT EXISTS idx_compra_detalle_producto ON public.compra_detalle(producto_id);

-- 13. HISTORIAL PEDIDOS (por bodega, tabla desnormalizada para reportes rápidos)
CREATE TABLE IF NOT EXISTS public.historial_pedidos (
    id_historial_ventas BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES public.ventas(id),
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    sector_id BIGINT REFERENCES public.sectores(id),
    nombre_producto VARCHAR(200) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(12, 2),
    subtotal DECIMAL(12, 2),
    fecha TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_historial_pedidos_venta ON public.historial_pedidos(venta_id);
CREATE INDEX IF NOT EXISTS idx_historial_pedidos_sector ON public.historial_pedidos(sector_id);
CREATE INDEX IF NOT EXISTS idx_historial_pedidos_fecha ON public.historial_pedidos(fecha);

-- 14. MOVIMIENTOS DE INVENTARIO (trazabilidad de entradas, salidas y ajustes)
CREATE TABLE IF NOT EXISTS public.movimientos_inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES public.productos(id),
    tipo VARCHAR(20) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    motivo VARCHAR(200),
    referencia_id BIGINT,
    usuario_id BIGINT REFERENCES public.usuarios(id),
    sector_id BIGINT REFERENCES public.sectores(id),
    fecha TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_mov_inv_producto ON public.movimientos_inventario(producto_id);
CREATE INDEX IF NOT EXISTS idx_mov_inv_sector ON public.movimientos_inventario(sector_id);
CREATE INDEX IF NOT EXISTS idx_mov_inv_fecha ON public.movimientos_inventario(fecha);
CREATE INDEX IF NOT EXISTS idx_mov_inv_tipo ON public.movimientos_inventario(tipo);
