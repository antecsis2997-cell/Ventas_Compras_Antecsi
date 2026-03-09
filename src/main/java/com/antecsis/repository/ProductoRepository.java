package com.antecsis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.antecsis.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByStockLessThanEqual(Integer stock, Pageable pageable);

    Page<Producto> findBySectorId(Long sectorId, Pageable pageable);

    Page<Producto> findBySectorIdAndStockLessThanEqual(Long sectorId, Integer stock, Pageable pageable);

    @Query("""
        SELECT p FROM Producto p WHERE p.stockMinimoAlerta IS NOT NULL AND p.stock <= p.stockMinimoAlerta
        """)
    Page<Producto> findByStockBajoPorAlerta(Pageable pageable);

    @Query("""
        SELECT p FROM Producto p WHERE p.stockMinimoAlerta IS NOT NULL
        AND p.stock <= p.stockMinimoAlerta AND p.sector.id = :sectorId
        """)
    Page<Producto> findByStockBajoPorAlertaAndSectorId(@Param("sectorId") Long sectorId, Pageable pageable);

    boolean existsByNombreAndSectorId(String nombre, Long sectorId);

    boolean existsByCodigoAndSectorId(String codigo, Long sectorId);
}
