package com.antecsis.service.impl;

import com.antecsis.dto.CompraItemDTO;
import com.antecsis.dto.CompraRequestDTO;
import com.antecsis.dto.CompraResponseDTO;
import com.antecsis.entity.Compra;
import com.antecsis.entity.CompraDetalle;
import com.antecsis.entity.Producto;
import com.antecsis.repository.CompraRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.service.CompraService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepo;
    private final ProductoRepository productoRepo;

    public CompraServiceImpl(CompraRepository compraRepo, ProductoRepository productoRepo) {
        this.compraRepo = compraRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public CompraResponseDTO crear(CompraRequestDTO dto) {
        Compra compra = new Compra();
        compra.setProveedor(dto.getProveedor());
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
                guardada.getProveedor(),
                guardada.getFecha(),
                guardada.getTotal()
        );
    }

    @Override
    public List<CompraResponseDTO> listar() {
        return compraRepo.findAll().stream()
                .map(c -> new CompraResponseDTO(
                        c.getId(),
                        c.getProveedor(),
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
                c.getId(), c.getProveedor(), c.getFecha(), c.getTotal()
        );
    }
}
