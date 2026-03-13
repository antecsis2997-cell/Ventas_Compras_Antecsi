package com.antecsis.controller;

import java.util.List;

import com.antecsis.dto.login.AprobarRecuperacionResponseDTO;
import com.antecsis.dto.login.PuedeRecuperarRequestDTO;
import com.antecsis.dto.login.PuedeRecuperarResponseDTO;
import com.antecsis.dto.login.ForgotPasswordRequestDTO;
import com.antecsis.dto.login.LoginRequestDTO;
import com.antecsis.dto.login.LoginResponseDTO;
import com.antecsis.dto.login.MeResponseDTO;
import com.antecsis.dto.login.RefreshRequestDTO;
import com.antecsis.dto.login.ResetPasswordRequestDTO;
import com.antecsis.dto.login.SolicitudRecuperacionResponseDTO;
import com.antecsis.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@Tag(name = "Auth", description = "Login, refresh token y autenticación JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @Operation(summary = "Iniciar sesión", description = "Devuelve access token y refresh token. No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Usuario/contraseña incorrectos o datos inválidos")
    })
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return service.login(dto.username(), dto.password());
    }

    @Operation(summary = "Renovar token", description = "Devuelve nuevo access token y refresh token usando el refresh token actual. No requiere Bearer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return service.refresh(dto.refreshToken());
    }

    @Operation(summary = "Usuario actual", description = "Devuelve el usuario autenticado (rol y sede). Requiere Bearer token.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = MeResponseDTO.class)))
    @GetMapping("/me")
    public ResponseEntity<MeResponseDTO> me() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @Operation(summary = "¿Puede recuperar contraseña?", description = "Indica si el usuario puede ver el link Recuperar contraseña tras fallar login. Público.")
    @PostMapping("/puede-recuperar")
    public ResponseEntity<PuedeRecuperarResponseDTO> puedeRecuperar(@Valid @RequestBody PuedeRecuperarRequestDTO dto) {
        return ResponseEntity.ok(new PuedeRecuperarResponseDTO(service.puedeRecuperarContrasena(dto.username())));
    }

    @Operation(summary = "Solicitar recuperación de contraseña", description = "Crea una solicitud pendiente. Admin o Soporte del sector debe aprobar para enviar el correo. No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Si el correo existe, se creó la solicitud"),
            @ApiResponse(responseCode = "400", description = "Correo inválido")
    })
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        service.solicitarRecuperacion(dto.correo());
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','SOPORTE')")
    @Operation(summary = "Listar solicitudes pendientes", description = "Admin, Soporte o Superusuario: listan solicitudes de recuperación pendientes de aprobar.")
    @GetMapping("/solicitudes-recuperacion")
    public ResponseEntity<List<SolicitudRecuperacionResponseDTO>> listarSolicitudesPendientes() {
        return ResponseEntity.ok(service.listarSolicitudesPendientes());
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','SOPORTE')")
    @Operation(summary = "Aprobar solicitud y enviar correo", description = "Admin/Soporte aprueba la solicitud; se envía el correo de recuperación al usuario.")
    @PostMapping("/aprobar-recuperacion/{id}")
    public ResponseEntity<AprobarRecuperacionResponseDTO> aprobarRecuperacion(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprobarRecuperacion(id));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','SOPORTE')")
    @Operation(summary = "Enviar recuperación directa", description = "Admin/Soporte envía correo de recuperación directamente a un usuario.")
    @PostMapping("/enviar-recuperacion/{usuarioId}")
    public ResponseEntity<AprobarRecuperacionResponseDTO> enviarRecuperacion(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.enviarRecuperacionDirecta(usuarioId));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','SOPORTE')")
    @Operation(summary = "Rechazar solicitud", description = "Admin/Soporte rechaza la solicitud de recuperación.")
    @PostMapping("/rechazar-recuperacion/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rechazarRecuperacion(@PathVariable Long id) {
        service.rechazarRecuperacion(id);
    }

    @Operation(summary = "Restablecer contraseña", description = "Establece la nueva contraseña usando el token del correo. No requiere autenticación.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado")
    })
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        service.resetPassword(dto.token(), dto.nuevaContrasena());
    }
}
