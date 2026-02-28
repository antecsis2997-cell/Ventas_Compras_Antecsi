package com.antecsis.repository;

import com.antecsis.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    Page<MovimientoInventario> findBySectorId(Long sectorId, Pageable pageable);

    Page<MovimientoInventario> findByProductoId(Long productoId, Pageable pageable);

    Page<MovimientoInventario> findByProductoIdAndSectorId(Long productoId, Long sectorId, Pageable pageable);
}
