package com.antecsis.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByStockLessThanEqual(Integer stock);
}
