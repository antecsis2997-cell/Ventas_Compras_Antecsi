package com.antecsis.repository;

import com.antecsis.entity.CompraDetalle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {

    @Query("""
            SELECT cd.producto.id, cd.producto.nombre, SUM(cd.cantidad)
            FROM CompraDetalle cd
            GROUP BY cd.producto.id, cd.producto.nombre
            ORDER BY SUM(cd.cantidad) DESC
        """)
    List<Object[]> productoMasComprado();
}
