package com.antecsis.controller;

import com.antecsis.dto.producto.ProductoRequestDTO;
import com.antecsis.dto.producto.ProductoResponseDTO;
import com.antecsis.service.InsumoService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Insumos", description = "CRUD insumos (materia prima).")
@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService service;

    @Operation(summary = "Crear insumo", description = "Alta de insumo. Se registra como esInsumo=true.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo creado"),
            @ApiResponse(responseCode = "400", description = "Validación fallida o duplicado")
    })
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(summary = "Listar insumos", description = "Listado paginado de insumos.")
    @GetMapping
    public ResponseEntity<Page<ProductoResponseDTO>> listar(Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @Operation(summary = "Obtener insumo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtener(
            @Parameter(description = "ID del insumo") @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar insumo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo actualizado"),
            @ApiResponse(responseCode = "404", description = "Insumo no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @Parameter(description = "ID del insumo") @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar insumo", description = "Eliminación lógica (activo=false).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

