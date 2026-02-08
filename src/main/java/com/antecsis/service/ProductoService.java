package com.antecsis.service;

import java.util.List;

import com.antecsis.dto.ProductoRequestDTO;
import com.antecsis.dto.ProductoResponseDTO;

public interface ProductoService {
	ProductoResponseDTO crear(ProductoRequestDTO producto);
	List<ProductoResponseDTO> listar();
}
