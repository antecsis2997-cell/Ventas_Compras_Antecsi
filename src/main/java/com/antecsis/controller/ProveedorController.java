package com.antecsis.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.antecsis.entity.Proveedor;
import com.antecsis.service.ProveedorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
	private final ProveedorService proveedorService;


    @PostMapping
    public Proveedor crear(@RequestBody Proveedor proveedor) {
        return proveedorService.crear(proveedor);
    }

    @GetMapping
    public List<Proveedor> listar() {
        return proveedorService.listar();
    }
}
