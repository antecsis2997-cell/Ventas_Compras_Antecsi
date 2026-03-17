package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;

public interface VentaService {
    VentaResponseDTO crear(VentaRequestDTO dto);
    /** Vista previa del siguiente número de comprobante para la sede del usuario (no consume la secuencia). */
    String siguienteNumeroComprobantePreview(String tipoDocumento);
    Page<VentaResponseDTO> listar(Pageable pageable, Long sectorId);
    VentaResponseDTO obtenerPorId(Long id);
    VentaResponseDTO anular(Long id);

    Page<VentaResponseDTO> listarEntregasPendientes(Pageable pageable, Long sectorId, String tipoEntrega);
    VentaResponseDTO marcarEntregado(Long ventaId);
    java.util.List<com.antecsis.dto.logistica.MetricasEntregasVendedorDTO> metricasEntregasPorVendedor(Long sectorId);
    VentaResponseDTO solicitarTracking(Long ventaId);
    VentaResponseDTO confirmarEntrega(Long ventaId, com.antecsis.dto.venta.ConfirmacionEntregaRequestDTO dto);

    java.util.List<com.antecsis.dto.logistica.LogisticaEntregaDetalleDTO> metricasLogisticaEntregas(
            Long sectorId,
            Long vendedorId,
            String distrito,
            String provincia,
            String pais
    );
}
