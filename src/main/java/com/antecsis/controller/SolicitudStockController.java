package com.antecsis.controller;

import com.antecsis.dto.solicitudstock.SolicitudStockRequestDTO;
import com.antecsis.dto.solicitudstock.SolicitudStockResponseDTO;
import com.antecsis.entity.Usuario;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.SolicitudStockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Solicitudes Stock", description = "Solicitudes de adquisición de stock. Cajero envía, Logística aprueba/desaprueba.")
@RestController
@RequestMapping("/api/solicitudes-stock")
@RequiredArgsConstructor
public class SolicitudStockController {

    private final SolicitudStockService service;
    private final UsuarioRepository usuarioRepository;

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','CAJERO')")
    @PostMapping
    public ResponseEntity<SolicitudStockResponseDTO> crear(@Valid @RequestBody SolicitudStockRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Page<SolicitudStockResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','LOGISTICA')")
    @PostMapping("/{id}/aprobar")
    public ResponseEntity<SolicitudStockResponseDTO> aprobar(@PathVariable Long id) {
        return ResponseEntity.ok(service.aprobar(id));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','LOGISTICA')")
    @PostMapping("/{id}/desaprobar")
    public ResponseEntity<SolicitudStockResponseDTO> desaprobar(@PathVariable Long id) {
        return ResponseEntity.ok(service.desaprobar(id));
    }

    @PreAuthorize("hasAnyRole('SUPERUSUARIO','ADMIN','CAJERO')")
    @GetMapping("/usuarios-por-correo")
    public ResponseEntity<List<Map<String, String>>> usuariosPorCorreo(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<Usuario> users = usuarioRepository.findTop10ByCorreoContainingIgnoreCaseOrderByCorreo(q.trim());
        List<Map<String, String>> result = users.stream()
                .filter(u -> u.getCorreo() != null && !u.getCorreo().isBlank())
                .map(u -> Map.of(
                        "correo", u.getCorreo(),
                        "nombre", (u.getNombre() != null ? u.getNombre() : "") + " " + (u.getApellido() != null ? u.getApellido() : "").trim()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
