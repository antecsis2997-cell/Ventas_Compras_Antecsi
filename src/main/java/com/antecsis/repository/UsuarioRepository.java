package com.antecsis.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	/** Lista por rol (paginado). */
	Page<Usuario> findByRol_Nombre(String rolNombre, Pageable pageable);

	/**
	 * Vista plataforma: administradores de bodega (ADMIN) y superusuarios cliente
	 * (SUPERUSUARIO con al menos una bodega en licencia). No incluye la cuenta plataforma SUPERUSUARIO sin bodegas gestionadas.
	 */
	@Query("""
			SELECT u FROM Usuario u
			WHERE u.rol.nombre = :admin
			   OR (u.rol.nombre = :superusuario AND SIZE(u.sectoresGestionados) > 0)
			ORDER BY LOWER(u.username)
			""")
	Page<Usuario> findAdminsYClientesSuperusuario(
			@Param("admin") String admin,
			@Param("superusuario") String superusuario,
			Pageable pageable);

	Page<Usuario> findBySede_IdIn(Collection<Long> sedeIds, Pageable pageable);

	List<Usuario> findBySede_IdAndRol_Nombre(Long sedeId, String rolNombre);

	/** Usuarios (p. ej. SUPERUSUARIO cliente) que tienen esta bodega en su lista gestionada. */
	List<Usuario> findBySectoresGestionados_Id(Long sectorId);
}
