package com.antecsis.service;

import java.util.List;
import java.util.Optional;

import com.antecsis.entity.Proveedor;

public interface ProveedorService {
	Proveedor crear(Proveedor proveedor);

	Optional<Proveedor> obtenerPorId(Long id);

    List<Proveedor> listar();
}
