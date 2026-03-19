package com.antecsis.service;

import com.antecsis.dto.producto.ProductoRequestDTO;
import com.antecsis.dto.producto.ProductoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InsumoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    Page<ProductoResponseDTO> listar(Pageable pageable);
    ProductoResponseDTO obtenerPorId(Long id);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void eliminar(Long id);
}

