package com.antecsis.repository;

import com.antecsis.entity.VentaDetalle;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {
	@Query("""
	        SELECT vd.producto.id, vd.producto.nombre, SUM(vd.cantidad)
	        FROM VentaDetalle vd
	        GROUP BY vd.producto.id, vd.producto.nombre
	        ORDER BY SUM(vd.cantidad) DESC
	    """)
	List<Object[]> productoMasVendido();
}