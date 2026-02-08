package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.producto.ProductoRequestDTO;
import com.antecsis.dto.producto.ProductoResponseDTO;

public interface ProductoService {
	ProductoResponseDTO crear(ProductoRequestDTO producto);
	List<ProductoResponseDTO> listar();
}
