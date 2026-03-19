package com.antecsis.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Datos para registrar una venta: cliente, tipo documento (FACTURA/BOLETA), items (productoId, cantidad)")
public record VentaRequestDTO(
    @Schema(description = "ID del cliente", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Cliente es obligatorio")
    Long clienteId,

    @Schema(description = "ID del método de pago (opcional)")
    Long metodoPagoId,

    @Schema(description = "Tipo de documento: FACTURA o BOLETA", allowableValues = { "FACTURA", "BOLETA" })
    String tipoDocumento,

    @Schema(description = "Número de factura o boleta (ej. F001-00001)")
    String numeroDocumento,

    @Schema(description = "Observaciones")
    String observaciones,

    @Schema(description = "Moneda: PEN o USD", allowableValues = { "PEN", "USD" })
    String moneda,

    @Schema(description = "Si el pago con tarjeta es con cuotas (true) o sin cuotas (false)")
    Boolean conCuotas,

    @Schema(description = "Si requiere delivery. Si true, venta queda PENDIENTE hasta que Logística marque entregado.")
    Boolean requiereDelivery,

    @Schema(description = "Tipo de entrega: INMEDIATA o PROGRAMADA_3_5 (3 a 5 días)", allowableValues = { "INMEDIATA", "PROGRAMADA_3_5" })
    String tipoEntrega,

    @Schema(description = "Dirección de entrega (obligatorio cuando tipoEntrega es INMEDIATA)")
    String direccionEntrega,

    @Schema(description = "DNI para acumulación de puntos CMR (opcional)")
    String dniCmr,

    @Schema(description = "Celular del cliente para pago con Yape (solo cuando el método de pago es Yape por PayU)")
    String yapeTelefono,

    @Schema(description = "Código OTP de Yape (6 dígitos)")
    String yapeOtp,

    @Schema(description = "Lista de items: productoId y cantidad. Al menos uno requerido.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Debe incluir al menos un item")
    List<@Valid VentaItemDTO> items
) {}
