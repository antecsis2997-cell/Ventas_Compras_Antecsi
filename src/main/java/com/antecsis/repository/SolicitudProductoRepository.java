package com.antecsis.repository;

import com.antecsis.entity.SolicitudProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudProductoRepository extends JpaRepository<SolicitudProducto, Long> {
    Page<SolicitudProducto> findByOrderByFechaDesc(Pageable pageable);
    List<SolicitudProducto> findByAtendidaFalseOrderByFechaDesc();
}
