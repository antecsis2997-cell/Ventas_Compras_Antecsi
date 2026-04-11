package com.antecsis.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.antecsis.entity.MetodoPago;
import com.antecsis.entity.Modulo;
import com.antecsis.entity.Rol;
import com.antecsis.entity.RubroComercial;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.repository.MetodoPagoRepository;
import com.antecsis.repository.ModuloRepository;
import com.antecsis.repository.RolRepository;
import com.antecsis.repository.RubroComercialRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            MetodoPagoRepository metodoPagoRepository,
            SectorRepository sectorRepository,
            RubroComercialRepository rubroComercialRepository,
            ModuloRepository moduloRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ── Sector por defecto (documento: Sector -> Nombre_Sector, Telefono, Direccion) ──
            if (sectorRepository.count() == 0) {
                Sector sector = new Sector();
                sector.setNombreSector("Principal");
                sector.setTelefono("");
                sector.setDireccion("");
                sectorRepository.save(sector);
                log.info("Sector Principal creado");
            }
            // ── Roles según documento: SUPERUSUARIO, ADMIN, CAJERO, ALMACENERO, VENTAS, LOGISTICA, ADMINISTRACION ──
            Rol superusuarioRol = crearRolSiNoExiste(rolRepository, "SUPERUSUARIO");
            crearRolSiNoExiste(rolRepository, "ADMIN");
            crearRolSiNoExiste(rolRepository, "CAJERO");
            crearRolSiNoExiste(rolRepository, "ALMACENERO");
            crearRolSiNoExiste(rolRepository, "VENTAS");
            crearRolSiNoExiste(rolRepository, "LOGISTICA");
            crearRolSiNoExiste(rolRepository, "ADMINISTRACION");
            crearRolSiNoExiste(rolRepository, "SOPORTE");


            // ── Superusuario (dueño del software): único que puede crear admins y cajeros ──
            if (usuarioRepository.findByUsername("superadmin").isEmpty()) {
                Usuario superadmin = new Usuario();
                superadmin.setUsername("superadmin");
                superadmin.setPassword(passwordEncoder.encode("superadmin123"));
                superadmin.setNombre("Super");
                superadmin.setApellido("Administrador");
                superadmin.setCorreo("superadmin@antecsis.com");
                superadmin.setActivo(true);
                superadmin.setRol(superusuarioRol);
                usuarioRepository.save(superadmin);
                log.info("Superusuario (dueño del software) creado: superadmin / superadmin123");
            }

            // ── Métodos de pago ──
            crearMetodoPagoSiNoExiste(metodoPagoRepository, "EFECTIVO");
            crearMetodoPagoSiNoExiste(metodoPagoRepository, "TARJETA");
            crearMetodoPagoSiNoExiste(metodoPagoRepository, "TRANSFERENCIA");
            crearMetodoPagoSiNoExiste(metodoPagoRepository, "YAPE");
            crearMetodoPagoSiNoExiste(metodoPagoRepository, "PLIN");

            // ── Rubros comerciales (clasificación del negocio; distinto del sector/sede) ──
            crearRubroComercialSiNoExiste(rubroComercialRepository, "MERCADO", "Mercado y retail", 1);
            crearRubroComercialSiNoExiste(rubroComercialRepository, "ZAPATERIA", "Zapaterías", 2);
            crearRubroComercialSiNoExiste(rubroComercialRepository, "ROPA", "Tienda de ropa", 3);
            crearRubroComercialSiNoExiste(rubroComercialRepository, "ALIMENTOS", "Alimentos y bebidas", 4);
            crearRubroComercialSiNoExiste(rubroComercialRepository, "OTROS", "Otros sectores", 5);

            // ── Módulos del sistema (catálogo para permisos por usuario) ──
            crearModuloSiNoExiste(moduloRepository, "DASHBOARD",            "Dashboard",            "Panel principal con resumen",              "dashboard",         1);
            crearModuloSiNoExiste(moduloRepository, "VENTAS",               "Ventas",               "Registro y gestión de ventas",             "shopping_cart",     2);
            crearModuloSiNoExiste(moduloRepository, "COMPRAS",              "Compras",              "Registro y gestión de compras",            "shopping_bag",      3);
            crearModuloSiNoExiste(moduloRepository, "PRODUCTOS",            "Productos",            "Catálogo de productos",                    "inventory_2",       4);
            crearModuloSiNoExiste(moduloRepository, "INVENTARIO",           "Inventario",           "Control de stock e inventario",            "warehouse",         5);
            crearModuloSiNoExiste(moduloRepository, "CLIENTES",             "Clientes",             "Gestión de clientes",                     "people",            6);
            crearModuloSiNoExiste(moduloRepository, "PROVEEDORES",          "Proveedores",          "Gestión de proveedores",                  "local_shipping",    7);
            crearModuloSiNoExiste(moduloRepository, "REPORTES",             "Reportes",             "Reportes y estadísticas",                 "assessment",        8);
            crearModuloSiNoExiste(moduloRepository, "SOLICITUDES_STOCK",    "Solicitudes de Stock", "Solicitudes de reposición de stock",      "assignment",        9);
            crearModuloSiNoExiste(moduloRepository, "SOLICITUDES_PRODUCTO", "Solicitudes de Producto", "Solicitudes de nuevos productos",      "note_add",         10);
            crearModuloSiNoExiste(moduloRepository, "CATEGORIAS",           "Categorías",           "Gestión de categorías de productos",      "category",         11);
            crearModuloSiNoExiste(moduloRepository, "METODOS_PAGO",         "Métodos de Pago",      "Configuración de métodos de pago",        "payment",          12);
            crearModuloSiNoExiste(moduloRepository, "HISTORIAL_PEDIDOS",    "Historial de Pedidos", "Historial de pedidos realizados",         "history",          13);
            crearModuloSiNoExiste(moduloRepository, "MENSAJES",             "Mensajes",             "Mensajería interna",                      "mail",             14);
            crearModuloSiNoExiste(moduloRepository, "USUARIOS",             "Usuarios",             "Gestión de usuarios y permisos",          "manage_accounts",  15);
            crearModuloSiNoExiste(moduloRepository, "LOGISTICA_ENTREGAS",   "Entregas y Delivery",  "Gestión de entregas y delivery",           "local_shipping",   16);

            migrarRolPlataformaASuperadmin(rolRepository);
        };
    }

    /**
     * Antes el dueño de la solución usaba el nombre SUPERUSUARIO; ahora es SUPERADMIN.
     * El nombre SUPERUSUARIO queda reservado al cliente multi-bodega.
     */
    private void migrarRolPlataformaASuperadmin(RolRepository rolRepository) {
        if (rolRepository.findByNombre("SUPERADMIN").isEmpty()) {
            rolRepository.findByNombre("SUPERUSUARIO").ifPresent(r -> {
                r.setNombre("SUPERADMIN");
                rolRepository.save(r);
                log.info("Migración: rol SUPERUSUARIO (plataforma) renombrado a SUPERADMIN");
            });
        }
        crearRolSiNoExiste(rolRepository, "SUPERUSUARIO");
    }

    private Rol crearRolSiNoExiste(RolRepository repo, String nombre) {
        return repo.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol();
            r.setNombre(nombre);
            log.info("Rol {} creado", nombre);
            return repo.save(r);
        });
    }

    private void crearMetodoPagoSiNoExiste(MetodoPagoRepository repo, String nombre) {
        if (!repo.existsByNombre(nombre)) {
            MetodoPago mp = new MetodoPago();
            mp.setNombre(nombre);
            mp.setActivo(true);
            repo.save(mp);
            log.info("Método de pago {} creado", nombre);
        }
    }

    private void crearRubroComercialSiNoExiste(RubroComercialRepository repo, String codigo, String nombre, int orden) {
        if (repo.findByCodigoIgnoreCase(codigo).isEmpty()) {
            RubroComercial r = RubroComercial.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .activo(true)
                    .orden(orden)
                    .build();
            repo.save(r);
            log.info("Rubro comercial {} creado", codigo);
        }
    }

    private void crearModuloSiNoExiste(ModuloRepository repo, String codigo, String nombre,
                                        String descripcion, String icono, int orden) {
        if (repo.findByCodigo(codigo).isEmpty()) {
            Modulo m = new Modulo();
            m.setCodigo(codigo);
            m.setNombre(nombre);
            m.setDescripcion(descripcion);
            m.setIcono(icono);
            m.setOrden(orden);
            m.setActivo(true);
            repo.save(m);
            log.info("Módulo {} creado", codigo);
        }
    }
}
