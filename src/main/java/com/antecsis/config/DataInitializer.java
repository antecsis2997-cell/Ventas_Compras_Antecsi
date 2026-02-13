package com.antecsis.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.antecsis.entity.MetodoPago;
import com.antecsis.entity.Rol;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.repository.MetodoPagoRepository;
import com.antecsis.repository.RolRepository;
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
        };
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
}
