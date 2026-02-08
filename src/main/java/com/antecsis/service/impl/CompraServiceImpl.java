package com.antecsis.service.impl;

import com.antecsis.dto.compra.CompraItemDTO;
import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;
import com.antecsis.entity.Compra;
import com.antecsis.entity.CompraDetalle;
import com.antecsis.entity.Producto;
import com.antecsis.entity.Proveedor;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.CompraRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.ProveedorRepository;
import com.antecsis.service.CompraService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;

    public CompraServiceImpl(CompraRepository compraRepo, ProductoRepository productoRepo, ProveedorRepository proveedorRepo) {
        this.compraRepo = compraRepo;
        this.productoRepo = productoRepo;
        this.proveedorRepo = proveedorRepo;
    }

    @Override
    public CompraResponseDTO crear(CompraRequestDTO dto) {
        Compra compra = new Compra();
        
        Proveedor proveedor = proveedorRepo.findById(dto.getProveedorId())
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));
        compra.setProveedor(proveedor);
        compra.setFecha(LocalDateTime.now());
        List<CompraDetalle> detalles = new ArrayList<>();
        double total = 0;

        for (CompraItemDTO item : dto.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no existe"));

            // SUMA stock
            producto.setStock(producto.getStock() + item.getCantidad());
            productoRepo.save(producto);

            CompraDetalle det = new CompraDetalle();
            det.setCompra(compra);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(item.getPrecioUnitario());

            total += item.getCantidad() * item.getPrecioUnitario();
            detalles.add(det);
        }

        compra.setTotal(total);
        compra.setDetalles(detalles);

        Compra guardada = compraRepo.save(compra);

        return new CompraResponseDTO(
        	    guardada.getId(),
        	    guardada.getProveedor().getId(),
        	    guardada.getProveedor().getNombre(),
        	    guardada.getFecha(),
        	    guardada.getTotal()
        	);
    }

    @Override
    public List<CompraResponseDTO> listar() {
        return compraRepo.findAll().stream()
        		.map(c -> new CompraResponseDTO(
        			    c.getId(),
        			    c.getProveedor().getId(),
        			    c.getProveedor().getNombre(),
        			    c.getFecha(),
        			    c.getTotal()
        			))
                .toList();
    }

    @Override
    public CompraResponseDTO obtenerPorId(Long id) {
        Compra c = compraRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no existe"));

        return new CompraResponseDTO(
        	    c.getId(),
        	    c.getProveedor().getId(),
        	    c.getProveedor().getNombre(),
        	    c.getFecha(),
        	    c.getTotal()
        	);
    }
}
