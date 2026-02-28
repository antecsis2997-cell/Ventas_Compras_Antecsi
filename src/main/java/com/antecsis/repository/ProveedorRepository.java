package com.antecsis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByNombre(String nombre);

    boolean existsByNombreAndSectorId(String nombre, Long sectorId);

    Page<Proveedor> findBySectorId(Long sectorId, Pageable pageable);
}
