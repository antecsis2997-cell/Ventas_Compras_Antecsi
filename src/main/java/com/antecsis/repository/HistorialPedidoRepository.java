package com.antecsis.repository;

import com.antecsis.entity.HistorialPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Long> {

    List<HistorialPedido> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    List<HistorialPedido> findByFechaBetweenAndSectorIdOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin, Long sectorId);

    Page<HistorialPedido> findByOrderByFechaDesc(Pageable pageable);

    Page<HistorialPedido> findBySectorIdOrderByFechaDesc(Long sectorId, Pageable pageable);
}
