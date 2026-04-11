package com.antecsis.controller;

import com.antecsis.dto.logistica.MetricasEntregasVendedorDTO;
import com.antecsis.dto.logistica.LogisticaEntregaDetalleDTO;
import com.antecsis.dto.venta.ConfirmacionEntregaRequestDTO;
import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ventas", description = "Registro de ventas, listado, anulación (Factura/Boleta)")
@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService service;

    @Operation(summary = "Registrar venta", description = "Crea una venta con cliente, items (productoId, cantidad), tipo documento (FACTURA/BOLETA) y número. Requiere rol CAJERO, ADMIN o SUPERUSUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permiso")
    })
    @PostMapping
    public ResponseEntity<VentaResponseDTO> crear(@Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(summary = "Vista previa del siguiente número de comprobante", description = "Devuelve el número que se asignará al registrar (ej. B101-00000003). No consume la secuencia.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping("/siguiente-numero-comprobante")
    public ResponseEntity<java.util.Map<String, String>> siguienteNumeroComprobante(
            @Parameter(description = "BOLETA o FACTURA") @RequestParam String tipoDocumento) {
        String numero = service.siguienteNumeroComprobantePreview(tipoDocumento);
        return ResponseEntity.ok(java.util.Map.of("siguienteNumero", numero != null ? numero : ""));
    }

    @Operation(summary = "Listar ventas", description = "Listado paginado. Opcionalmente filtra por sector. Requiere CAJERO, ADMIN o SUPERUSUARIO.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping
    public ResponseEntity<Page<VentaResponseDTO>> listar(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.listar(pageable, sectorId));
    }

    @Operation(summary = "Obtener venta por ID")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> obtener(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Anular venta", description = "Cambia el estado a ANULADA. Solo SUPERUSUARIO o ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta anulada"),
            @ApiResponse(responseCode = "400", description = "Venta ya anulada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN')")
    @PatchMapping("/{id}/anular")
    public ResponseEntity<VentaResponseDTO> anular(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(service.anular(id));
    }

    @Operation(summary = "Listar entregas pendientes", description = "Ventas con delivery pendiente. tipoEntrega: INMEDIATA (Delivery) o PROGRAMADA_3_5 (Entregas).")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA','VENTAS')")
    @GetMapping("/entregas-pendientes")
    public ResponseEntity<Page<VentaResponseDTO>> listarEntregasPendientes(
            Pageable pageable,
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId,
            @Parameter(description = "INMEDIATA=Delivery, PROGRAMADA_3_5=Entregas (opcional)") @RequestParam(name = "tipoEntrega", required = false) String tipoEntrega) {
        return ResponseEntity.ok(service.listarEntregasPendientes(pageable, sectorId, tipoEntrega));
    }

    @Operation(summary = "Marcar entrega como entregada", description = "Cuando Logística completa la entrega, la venta pasa a COMPLETADA.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA','VENTAS')")
    @PatchMapping("/{id}/marcar-entregado")
    public ResponseEntity<VentaResponseDTO> marcarEntregado(@Parameter(description = "ID de la venta") @PathVariable Long id) {
        return ResponseEntity.ok(service.marcarEntregado(id));
    }

    @Operation(summary = "Métricas de entregas por vendedor", description = "Cantidad de entregas y monto total por vendedor. Para Logística.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA')")
    @GetMapping("/metricas-entregas-vendedor")
    public ResponseEntity<java.util.List<MetricasEntregasVendedorDTO>> metricasEntregasPorVendedor(
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId) {
        return ResponseEntity.ok(service.metricasEntregasPorVendedor(sectorId));
    }

    @Operation(
            summary = "Detalle de entregas para Logística",
            description = "Lista de entregas (delivery) con vendedor, cliente, producto, cantidad y zona (distrito/provincia/pais)."
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA')")
    @GetMapping("/logistica/entregas-detalle")
    public ResponseEntity<java.util.List<LogisticaEntregaDetalleDTO>> metricasLogisticaEntregas(
            @Parameter(description = "ID del sector (opcional)") @RequestParam(required = false) Long sectorId,
            @Parameter(description = "ID del vendedor (opcional)") @RequestParam(required = false) Long vendedorId,
            @Parameter(description = "Filtro por distrito (contiene, opcional)") @RequestParam(required = false) String distrito,
            @Parameter(description = "Filtro por provincia (contiene, opcional)") @RequestParam(required = false) String provincia,
            @Parameter(description = "Filtro por país (contiene, opcional)") @RequestParam(required = false) String pais
    ) {
        return ResponseEntity.ok(service.metricasLogisticaEntregas(sectorId, vendedorId, distrito, provincia, pais));
    }

    @Operation(summary = "Solicitar tracking", description = "Genera código de tracking para seguimiento del envío.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA','VENTAS')")
    @PostMapping("/{id}/solicitar-tracking")
    public ResponseEntity<VentaResponseDTO> solicitarTracking(@PathVariable Long id) {
        return ResponseEntity.ok(service.solicitarTracking(id));
    }

    @Operation(summary = "Confirmar entrega", description = "Registra la confirmación del cliente: firma digital, correo, teléfono.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','LOGISTICA','VENTAS')")
    @PostMapping("/{id}/confirmar-entrega")
    public ResponseEntity<VentaResponseDTO> confirmarEntrega(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody ConfirmacionEntregaRequestDTO dto) {
        return ResponseEntity.ok(service.confirmarEntrega(id, dto));
    }

    @Operation(
        summary = "Compras calificadas de un cliente",
        description = "Devuelve cuántas compras mayores a 50 soles tiene acumuladas el cliente en el sector del usuario autenticado. Útil para mostrar el progreso de la promoción antes de registrar una venta.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN','CAJERO','VENTAS')")
    @GetMapping("/clientes/{clienteId}/compras-calificadas")
    public ResponseEntity<java.util.Map<String, Object>> comprasCalificadas(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        long count = service.contarComprasCalificadas(clienteId);
        // visitaActual = count + 1. Descuento si visitaActual % 10 == 0, es decir count % 10 == 9.
        boolean proximaConDescuento = (count % 10) == 9;
        // Cuántas compras calificadas más (después de esta) hacen falta para el siguiente descuento.
        long faltanParaDescuento = proximaConDescuento ? 0L : (9 - (count % 10));
        return ResponseEntity.ok(java.util.Map.of(
            "comprasCalificadas", count,
            "proximaDescuento", proximaConDescuento,
            "faltanParaDescuento", faltanParaDescuento
        ));
    }
}
