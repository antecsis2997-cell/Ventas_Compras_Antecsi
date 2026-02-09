package com.antecsis.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.antecsis.entity.Rol;
import com.antecsis.entity.Usuario;
import com.antecsis.repository.RolRepository;
import com.antecsis.repository.UsuarioRepository;

@Configuration
public class DataInitializer {
	@Bean
    CommandLineRunner initData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            Rol adminRol = rolRepository.findByNombre("ADMIN")
                    .orElseGet(() -> {
                        Rol r = new Rol();
                        r.setNombre("ADMIN");
                        return rolRepository.save(r);
                    });

            if (usuarioRepository.findByUsername("admin").isEmpty()) {

                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setActivo(true);
                admin.setRol(adminRol);

                usuarioRepository.save(admin);

                System.out.println("✅ Usuario admin creado");
            } else {
                System.out.println("ℹ️ Usuario admin ya existe");
            }
        };
    }
}
