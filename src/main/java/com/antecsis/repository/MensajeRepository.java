package com.antecsis.repository;

import com.antecsis.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    Page<Mensaje> findByOrderByFechaDesc(Pageable pageable);
    Page<Mensaje> findByNombreReceptorOrderByFechaDesc(String nombreReceptor, Pageable pageable);
}
