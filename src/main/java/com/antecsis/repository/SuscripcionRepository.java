package com.antecsis.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.antecsis.entity.Suscripcion;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    Page<Suscripcion> findByOrderByFechaCaducidadAsc(Pageable pageable);
    Page<Suscripcion> findByEstado(String estado, Pageable pageable);
    List<Suscripcion> findByFechaCaducidadLessThanEqual(LocalDate fecha);

    List<Suscripcion> findByEstadoAndFechaCaducidadBetween(String estado, LocalDate inicio, LocalDate fin);

    Optional<Suscripcion> findFirstBySector_IdAndEstadoOrderByIdDesc(Long sectorId, String estado);

    @Query("""
            SELECT s FROM Suscripcion s
            WHERE (:estado IS NULL OR s.estado = :estado)
            AND (:rubroId IS NULL OR s.rubroComercial.id = :rubroId)
            ORDER BY s.fechaCaducidad ASC
            """)
    Page<Suscripcion> findFiltrada(
            @Param("estado") String estado,
            @Param("rubroId") Long rubroId,
            Pageable pageable);
}
