package com.antecsis.dto.suscripcion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Compra pública de plan (obtener el programa)")
public record CompraPublicaRequestDTO(
    @NotBlank(message = "Plan es obligatorio")
    @Schema(description = "BASICO, INTERMEDIO o AVANZADO")
    String plan,

    @NotBlank(message = "RUC es obligatorio")
    String ruc,

    @NotBlank(message = "Nombre del cliente/RUC es obligatorio")
    String nombreCliente,

    @NotBlank(message = "Correo del administrador del plan es obligatorio")
    @Email(message = "Correo inválido")
    @Schema(description = "Correo que recibirá la licencia y alertas")
    String correoAdministrador,

    @Schema(description = "Código de rubro: MERCADO, ZAPATERIA, ROPA, ALIMENTOS, OTROS (opcional)")
    String rubroCodigo,

    String nombreTitularTarjeta,
    String numeroTarjeta,
    String fechaCaducidadTarjeta,

    @Schema(description = "ID de sucursal (opcional)")
    Long sectorId
) {}
