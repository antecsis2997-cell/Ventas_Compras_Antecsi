package com.antecsis.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Page<Producto> findByStockLessThanEqual(Integer stock, Pageable pageable);
}
