package com.antecsis.service.sunat;

import lombok.Builder;
import lombok.Getter;

/**
 * Resultado del envío a SUNAT o consulta de CDR.
 */
@Getter
@Builder
public class SunatCdrResult {

    /** Código de respuesta SUNAT: "0"=aceptado, "2xxx"-"3xxx"=rechazado */
    private String codigoRespuesta;

    /** Descripción del CDR */
    private String descripcion;

    /** Para envíos asíncronos (boletas/resumen diario): ticket de consulta */
    private String ticket;

    /** true si fue aceptado (código == "0") */
    private boolean aceptado;

    /** true si fue aceptado con observaciones */
    private boolean conObservaciones;

    /** Observaciones adicionales de SUNAT */
    private String observaciones;

    public static SunatCdrResult aceptado(String descripcion) {
        return SunatCdrResult.builder()
                .codigoRespuesta("0")
                .descripcion(descripcion)
                .aceptado(true)
                .conObservaciones(false)
                .build();
    }

    public static SunatCdrResult rechazado(String codigo, String descripcion) {
        return SunatCdrResult.builder()
                .codigoRespuesta(codigo)
                .descripcion(descripcion)
                .aceptado(false)
                .conObservaciones(false)
                .build();
    }

    public static SunatCdrResult ticket(String ticketNum) {
        return SunatCdrResult.builder()
                .ticket(ticketNum)
                .aceptado(false)
                .build();
    }

    public static SunatCdrResult errorEnvio(String mensaje) {
        return SunatCdrResult.builder()
                .codigoRespuesta("ERROR")
                .descripcion(mensaje)
                .aceptado(false)
                .build();
    }
}
