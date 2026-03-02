package com.antecsis.repository;

import com.antecsis.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByEmailAndSectorId(String email, Long sectorId);

    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND (" +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "c.documento LIKE CONCAT('%', :q, '%'))")
    Page<Cliente> buscarPorNombreODocumento(@Param("q") String q, Pageable pageable);

    Page<Cliente> findBySectorId(Long sectorId, Pageable pageable);

    boolean existsByDocumentoAndSectorId(String documento, Long sectorId);

    @Query("SELECT c FROM Cliente c WHERE c.sector.id = :sectorId AND c.activo = true AND (" +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "c.documento LIKE CONCAT('%', :q, '%'))")
    Page<Cliente> buscarPorNombreODocumentoYSector(@Param("q") String q, @Param("sectorId") Long sectorId, Pageable pageable);
}
