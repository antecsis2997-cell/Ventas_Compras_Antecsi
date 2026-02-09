package com.antecsis.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.antecsis.entity.Proveedor;
import com.antecsis.repository.ProveedorRepository;
import com.antecsis.service.ProveedorService;

@Service
public class ProveedorServiceImpl implements ProveedorService{
	
	private final ProveedorRepository proveedorRepository;

    public ProveedorServiceImpl(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

	@Override
	public Proveedor crear(Proveedor proveedor) {
		if (proveedor.getNombre() == null || proveedor.getNombre().isBlank()) {
            throw new RuntimeException("El nombre del proveedor es obligatorio");
        }
        return proveedorRepository.save(proveedor);
	}

	@Override
    public Optional<Proveedor> obtenerPorId(Long id) {
        return proveedorRepository.findById(id);
    }

	@Override
	public List<Proveedor> listar() {
		return proveedorRepository.findAll();
	}



}
