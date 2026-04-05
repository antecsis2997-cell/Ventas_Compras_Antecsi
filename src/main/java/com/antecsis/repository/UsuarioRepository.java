package com.antecsis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);

	Optional<Usuario> findByCorreoIgnoreCase(String correo);

	/** Para reglas de licencias: contar usuarios secundarios por rol del usuario principal. */
	long countByUsuarioPrincipalIdAndRolNombre(Long usuarioPrincipalId, String rolNombre);

	/** Verifica si existe al menos un usuario asignado a este sector. */
	boolean existsBySede_Id(Long sedeId);

	/** Lista usuarios de una sede (para ADMIN: solo su punto). */
	Page<Usuario> findBySede_Id(Long sedeId, Pageable pageable);

	/** Cuenta admins en una sede (máx 1 por sede). */
	long countBySede_IdAndRol_Nombre(Long sedeId, String rolNombre);

	/** Buscar usuarios por correo (autocomplete remitente). */
	List<Usuario> findTop10ByCorreoContainingIgnoreCaseOrderByCorreo(String correo);

	/** Lista solo los usuarios con un rol específico (paginado). Usado por SUPERUSUARIO para ver solo ADMINs. */
	Page<Usuario> findByRol_Nombre(String rolNombre, Pageable pageable);

	List<Usuario> findBySede_IdAndRol_Nombre(Long sedeId, String rolNombre);
}
