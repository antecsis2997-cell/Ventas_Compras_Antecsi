package com.antecsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Productos con stock menor o igual a un valor
    List<Producto> findByStockLessThanEqual(Integer stock);
}
