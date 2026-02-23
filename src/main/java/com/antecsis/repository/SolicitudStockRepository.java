package com.antecsis.repository;

import com.antecsis.entity.EstadoSolicitudStock;
import com.antecsis.entity.SolicitudStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudStockRepository extends JpaRepository<SolicitudStock, Long> {
    Page<SolicitudStock> findByOrderByFechaCreacionDesc(Pageable pageable);
    List<SolicitudStock> findByEstadoOrderByFechaCreacionDesc(EstadoSolicitudStock estado);
}
