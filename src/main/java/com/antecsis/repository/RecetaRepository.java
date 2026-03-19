package com.antecsis.repository;

import com.antecsis.entity.Receta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Page<Receta> findBySectorId(Long sectorId, Pageable pageable);
}

