package com.antecsis.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.dto.rubro.RubroActivoPatchDTO;
import com.antecsis.dto.rubro.RubroComercialDTO;
import com.antecsis.service.RubroComercialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Rubros comerciales", description = "Catálogo de rubros (Mercado, Zapatería, etc.)")
@RestController
@RequestMapping("/api/rubros-comerciales")
@RequiredArgsConstructor
public class RubroComercialController {

    private final RubroComercialService service;

    @Operation(summary = "Listar rubros habilitados (público, ej. selector en compra de plan)")
    @GetMapping
    public List<RubroComercialDTO> listarActivos() {
        return service.listarActivos();
    }

    @Operation(summary = "Listar todos los rubros (superusuario)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public List<RubroComercialDTO> listarTodos() {
        return service.listarTodos();
    }

    @Operation(summary = "Habilitar o deshabilitar rubro")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<RubroComercialDTO> actualizarActivo(
            @PathVariable Long id,
            @Valid @RequestBody RubroActivoPatchDTO dto) {
        return ResponseEntity.ok(service.actualizarActivo(id, dto));
    }
}
