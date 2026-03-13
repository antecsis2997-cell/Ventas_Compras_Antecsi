package com.antecsis.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Suscripcion;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    Page<Suscripcion> findByOrderByFechaCaducidadAsc(Pageable pageable);
    Page<Suscripcion> findByEstado(String estado, Pageable pageable);
    List<Suscripcion> findByFechaCaducidadLessThanEqual(LocalDate fecha);
}
