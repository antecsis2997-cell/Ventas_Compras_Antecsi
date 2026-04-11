package com.antecsis.controller;

import com.antecsis.service.sunat.SunatVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "SUNAT", description = "Operaciones manuales para el SEE del Contribuyente")
@RestController
@RequestMapping("/api/sunat")
@RequiredArgsConstructor
public class SunatController {

    private final SunatVentaService sunatVentaService;

    @Operation(summary = "Reintentar envío de venta a SUNAT",
               description = "Solo funciona con ventas en estado ERROR_ENVIO. Lanza el envío en transacción separada.")
    @PreAuthorize("hasAnyRole('SUPERADMIN','SUPERUSUARIO','ADMIN')")
    @PostMapping("/ventas/{ventaId}/reintentar")
    public ResponseEntity<Map<String, String>> reintentar(@PathVariable Long ventaId) {
        sunatVentaService.reintentarEnvio(ventaId);
        return ResponseEntity.ok(Map.of("mensaje", "Reintento iniciado para venta #" + ventaId));
    }
}
