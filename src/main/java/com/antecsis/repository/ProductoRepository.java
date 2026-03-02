package com.antecsis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByStockLessThanEqual(Integer stock, Pageable pageable);

    Page<Producto> findBySectorId(Long sectorId, Pageable pageable);

    Page<Producto> findBySectorIdAndStockLessThanEqual(Long sectorId, Integer stock, Pageable pageable);

    boolean existsByNombreAndSectorId(String nombre, Long sectorId);

    boolean existsByCodigoAndSectorId(String codigo, Long sectorId);
}
