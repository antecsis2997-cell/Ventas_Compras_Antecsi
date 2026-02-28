package com.antecsis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombre(String nombre);

    boolean existsByNombreAndSectorId(String nombre, Long sectorId);

    Page<Categoria> findBySectorId(Long sectorId, Pageable pageable);
}
