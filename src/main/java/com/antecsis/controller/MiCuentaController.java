package com.antecsis.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.licencia.ActivarLicenciaRequestDTO;
import com.antecsis.dto.licencia.LicenciaEstadoResponseDTO;
import com.antecsis.dto.notificacion.NotificacionBandejaDTO;
import com.antecsis.service.LicenciaCuentaService;
import com.antecsis.service.NotificacionBandejaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Mi cuenta", description = "Licencia del plan y bandeja de notificaciones del sistema")
@RestController
@RequestMapping("/api/mi-cuenta")
@RequiredArgsConstructor
public class MiCuentaController {

    private final LicenciaCuentaService licenciaCuentaService;
    private final NotificacionBandejaService notificacionBandejaService;

    @Operation(summary = "Estado de licencia y plan para la sede del usuario")
    @GetMapping("/licencia")
    public LicenciaEstadoResponseDTO estadoLicencia() {
        return licenciaCuentaService.estadoMiCuenta();
    }

    @Operation(summary = "Activar licencia con el token recibido por correo")
    @PostMapping("/licencia/activar")
    public ResponseEntity<Void> activarLicencia(@Valid @RequestBody ActivarLicenciaRequestDTO dto) {
        licenciaCuentaService.activar(dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Bandeja de mensajes enviados al correo del usuario")
    @GetMapping("/bandeja")
    public List<NotificacionBandejaDTO> bandeja() {
        return notificacionBandejaService.listarParaUsuarioAutenticado();
    }
}
