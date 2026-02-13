package com.antecsis.dto.venta;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "Datos para registrar una venta: cliente, tipo documento (FACTURA/BOLETA), items (productoId, cantidad)")
@Getter
@Setter
public class VentaRequestDTO {

	@Schema(description = "ID del cliente", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "Cliente es obligatorio")
	private Long clienteId;

	@Schema(description = "ID del método de pago (opcional)")
	private Long metodoPagoId;

	@Schema(description = "Tipo de documento: FACTURA o BOLETA", allowableValues = { "FACTURA", "BOLETA" })
	private String tipoDocumento;

	@Schema(description = "Número de factura o boleta (ej. F001-00001)")
	private String numeroDocumento;

	@Schema(description = "Observaciones")
	private String observaciones;

	@Schema(description = "Lista de items: productoId y cantidad. Al menos uno requerido.", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "Debe incluir al menos un item")
	private List<@Valid VentaItemDTO> items;
}
