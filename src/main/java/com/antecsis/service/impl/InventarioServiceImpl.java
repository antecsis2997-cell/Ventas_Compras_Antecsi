package com.antecsis.service.impl;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.service.InventarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponseDTO> listarTodo(Pageable pageable) {
        return productoRepo.findAll(pageable)
                .map(p -> new InventarioResponseDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getStock()
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponseDTO> stockBajo(Integer limite, Pageable pageable) {
        return productoRepo.findByStockLessThanEqual(limite, pageable)
                .map(p -> new InventarioResponseDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getStock()
                ));
    }
}
