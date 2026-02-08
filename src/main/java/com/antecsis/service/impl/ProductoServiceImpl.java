package com.antecsis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.antecsis.dto.producto.ProductoRequestDTO;
import com.antecsis.dto.producto.ProductoResponseDTO;
import com.antecsis.entity.Producto;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.service.ProductoService;

@Service
public class ProductoServiceImpl implements ProductoService{
	
	private final ProductoRepository repository;

    public ProductoServiceImpl(ProductoRepository repository) {
        this.repository = repository;
    }

	@Override
	public ProductoResponseDTO crear(ProductoRequestDTO dto) {
		Producto producto = new Producto();
	    producto.setNombre(dto.getNombre());
	    producto.setPrecio(dto.getPrecio());
	    producto.setStock(dto.getStock());

	    Producto guardado = repository.save(producto);

	    return new ProductoResponseDTO(
	        guardado.getId(),
	        guardado.getNombre(),
	        guardado.getPrecio(),
	        guardado.getStock()
	    );
	}

	@Override
	public List<ProductoResponseDTO> listar() {
		return repository.findAll().stream()
                .map(p -> new ProductoResponseDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getPrecio(),
                        p.getStock()
                ))
                .toList();
	}

}
