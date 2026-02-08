package com.antecsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long>{
	boolean existsByNombre(String nombre);
}
