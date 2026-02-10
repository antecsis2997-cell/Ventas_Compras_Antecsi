package com.antecsis.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.antecsis.entity.MetodoPago;
import com.antecsis.entity.Rol;
import com.antecsis.entity.Usuario;
import com.antecsis.repository.MetodoPagoRepository;
import com.antecsis.repository.RolRepository;
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
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ── Roles ──
            Rol adminRol = crearRolSiNoExiste(rolRepository, "ADMIN");
            crearRolSiNoExiste(rolRepository, "CAJERO");
            crearRolSiNoExiste(rolRepository, "ALMACENERO");

            // ── Usuario admin por defecto ──
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setNombre("Administrador");
                admin.setApellido("Sistema");
                admin.setActivo(true);
                admin.setRol(adminRol);
                usuarioRepository.save(admin);
                log.info("Usuario admin creado exitosamente");
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
