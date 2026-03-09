package com.antecsis.repository;

import com.antecsis.entity.EstadoEntrega;
import com.antecsis.entity.EstadoVenta;
import com.antecsis.entity.TipoEntrega;
import com.antecsis.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByRequiereDeliveryTrueAndEstadoEntrega(EstadoEntrega estadoEntrega);

    List<Venta> findByFechaBetweenAndSectorId(LocalDateTime inicio, LocalDateTime fin, Long sectorId);

    Page<Venta> findBySectorId(Long sectorId, Pageable pageable);

    long countByEstadoAndFechaBetween(EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin);

    long countByEstadoAndFechaBetweenAndSectorId(EstadoVenta estado, LocalDateTime inicio, LocalDateTime fin, Long sectorId);

    Page<Venta> findByRequiereDeliveryTrueAndEstado(EstadoVenta estado, Pageable pageable);

    Page<Venta> findByRequiereDeliveryTrueAndEstadoAndSectorId(EstadoVenta estado, Long sectorId, Pageable pageable);

    Page<Venta> findByRequiereDeliveryTrueAndEstadoAndTipoEntrega(EstadoVenta estado, TipoEntrega tipoEntrega, Pageable pageable);

    Page<Venta> findByRequiereDeliveryTrueAndEstadoAndSectorIdAndTipoEntrega(EstadoVenta estado, Long sectorId, TipoEntrega tipoEntrega, Pageable pageable);

    @Query("""
        SELECT v FROM Venta v WHERE v.requiereDelivery = true AND v.tipoEntrega = :tipo
        AND (v.estado = :pendiente OR v.estadoEntrega = :entregado) ORDER BY v.fecha DESC
        """)
    Page<Venta> findEntregasConHistorial(@Param("tipo") TipoEntrega tipo, @Param("pendiente") EstadoVenta pendiente,
            @Param("entregado") EstadoEntrega entregado, Pageable pageable);

    @Query("""
        SELECT v FROM Venta v WHERE v.requiereDelivery = true AND v.tipoEntrega = :tipo
        AND v.sector.id = :sectorId AND (v.estado = :pendiente OR v.estadoEntrega = :entregado)
        ORDER BY v.fecha DESC
        """)
    Page<Venta> findEntregasConHistorialBySector(@Param("tipo") TipoEntrega tipo, @Param("sectorId") Long sectorId,
            @Param("pendiente") EstadoVenta pendiente, @Param("entregado") EstadoEntrega entregado, Pageable pageable);
}
