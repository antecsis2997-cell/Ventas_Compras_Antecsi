package com.antecsis.repository;

import com.antecsis.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    Page<Compra> findBySectorId(Long sectorId, Pageable pageable);
}
