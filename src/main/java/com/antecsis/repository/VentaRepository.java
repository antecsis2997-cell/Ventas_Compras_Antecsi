package com.antecsis.repository;

import com.antecsis.entity.EstadoVenta;
import com.antecsis.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByFechaBetweenAndSectorId(LocalDateTime inicio, LocalDateTime fin, Long sectorId);

    Page<Venta> findBySectorId(Long sectorId, Pageable pageable);

    long countByEstadoAndFechaBetween(EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin);

    long countByEstadoAndFechaBetweenAndSectorId(EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin, Long sectorId);
}
