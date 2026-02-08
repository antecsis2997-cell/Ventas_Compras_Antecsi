package com.antecsis.controller;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.service.InventarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {
    private final InventarioService service;

    public InventarioController(InventarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<InventarioResponseDTO> listarTodo() {
        return service.listarTodo();
    }

    @GetMapping("/stock-bajo")
    public List<InventarioResponseDTO> stockBajo(
            @RequestParam(defaultValue = "5") Integer limite) {
        return service.stockBajo(limite);
    }
}
