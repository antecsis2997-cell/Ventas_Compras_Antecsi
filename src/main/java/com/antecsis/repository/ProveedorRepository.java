package com.antecsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long>{
	boolean existsByNombre(String nombre);
}
