package com.antecsis.repository;

import com.antecsis.entity.EstadoEntrega;
import com.antecsis.entity.EstadoVenta;
import com.antecsis.entity.SunatEstadoCdr;
import com.antecsis.entity.TipoDocumentoVenta;
import com.antecsis.entity.TipoEntrega;
import com.antecsis.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.math.BigDecimal;
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

    @Query("""
        SELECT COUNT(v) FROM Venta v
        WHERE v.cliente.id = :clienteId
          AND (:sectorId IS NULL OR v.sector.id = :sectorId)
          AND v.estado <> :estadoExcluido
          AND COALESCE(v.totalBruto, v.total) > :minTotal
        """)
    long countComprasCalificadasParaPromocion(
            @Param("clienteId") Long clienteId,
            @Param("sectorId") Long sectorId,
            @Param("minTotal") BigDecimal minTotal,
            @Param("estadoExcluido") EstadoVenta estadoExcluido);

    @Query("""
        SELECT COUNT(v)
        FROM Venta v
        WHERE v.requiereDelivery = true
          AND v.estado = :estadoVenta
          AND (:sectorId IS NULL OR v.sector.id = :sectorId)
        """)
    long countRequiereDeliveryByEstadoVenta(
            @Param("sectorId") Long sectorId,
            @Param("estadoVenta") EstadoVenta estadoVenta
    );

    @Query("""
        SELECT COUNT(v)
        FROM Venta v
        WHERE v.requiereDelivery = true
          AND v.estado = :estadoVenta
          AND v.estadoEntrega = :estadoEntrega
          AND (:sectorId IS NULL OR v.sector.id = :sectorId)
        """)
    long countRequiereDeliveryByEstadoEntrega(
            @Param("sectorId") Long sectorId,
            @Param("estadoVenta") EstadoVenta estadoVenta,
            @Param("estadoEntrega") EstadoEntrega estadoEntrega
    );

    // ── Queries para SEE del Contribuyente (SUNAT) ───────────────────────

    @Query("""
        SELECT v FROM Venta v
        WHERE v.sector.id = :sectorId
          AND v.tipoDocumento = com.antecsis.entity.TipoDocumentoVenta.BOLETA
          AND v.sunatEstadoCdr = :estado
          AND v.fecha BETWEEN :inicio AND :fin
        ORDER BY v.fecha ASC
        """)
    List<Venta> findBoletasPendientesParaResumen(
            @Param("sectorId") Long sectorId,
            @Param("estado") SunatEstadoCdr estado,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fin") java.time.LocalDateTime fin
    );

    @Query("""
        SELECT v FROM Venta v
        WHERE v.tipoDocumento = com.antecsis.entity.TipoDocumentoVenta.BOLETA
          AND v.sunatEstadoCdr = :estado
          AND v.sunatTicket IS NOT NULL
        """)
    List<Venta> findBoletasConTicketPendiente(@Param("estado") SunatEstadoCdr estado);

    @Query("""
        SELECT v FROM Venta v
        WHERE v.sunatEstadoCdr = :estado
          AND v.sunatIntentos < :maxIntentos
        ORDER BY v.fecha ASC
        """)
    List<Venta> findVentasParaReintentar(
            @Param("estado") SunatEstadoCdr estado,
            @Param("maxIntentos") int maxIntentos
    );
}
