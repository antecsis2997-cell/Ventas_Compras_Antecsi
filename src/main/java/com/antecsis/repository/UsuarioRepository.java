package com.antecsis.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);

	/** Para reglas de licencias: contar usuarios secundarios por rol del usuario principal. */
	long countByUsuarioPrincipalIdAndRolNombre(Long usuarioPrincipalId, String rolNombre);

	/** Verifica si existe al menos un usuario asignado a este sector. */
	boolean existsBySede_Id(Long sedeId);

	/** Lista usuarios de una sede (para ADMIN: solo su punto). */
	Page<Usuario> findBySede_Id(Long sedeId, Pageable pageable);

	/** Cuenta admins en una sede (máx 1 por sede). */
	long countBySede_IdAndRol_Nombre(Long sedeId, String rolNombre);
}
