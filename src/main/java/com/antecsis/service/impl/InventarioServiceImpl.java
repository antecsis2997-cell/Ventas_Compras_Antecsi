package com.antecsis.service.impl;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.service.InventarioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepo;

    public InventarioServiceImpl(ProductoRepository productoRepo) {
        this.productoRepo = productoRepo;
    }

    @Override
    public List<InventarioResponseDTO> listarTodo() {
        return productoRepo.findAll().stream()
                .map(p -> new InventarioResponseDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getStock()
                ))
                .toList();
    }

    @Override
    public List<InventarioResponseDTO> stockBajo(Integer limite) {
        return productoRepo.findByStockLessThanEqual(limite).stream()
                .map(p -> new InventarioResponseDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getStock()
                ))
                .toList();
    }
}
