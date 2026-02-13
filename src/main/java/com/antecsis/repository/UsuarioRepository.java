package com.antecsis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);

	/** Para reglas de licencias: contar usuarios secundarios por rol del usuario principal. */
	long countByUsuarioPrincipalIdAndRolNombre(Long usuarioPrincipalId, String rolNombre);
}
