package com.antecsis.service.impl;

import com.antecsis.dto.venta.VentaItemDTO;
import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.entity.Cliente;
import com.antecsis.entity.Producto;
import com.antecsis.entity.Venta;
import com.antecsis.entity.VentaDetalle;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ClienteRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.VentaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;

    public VentaServiceImpl(VentaRepository ventaRepo,
                            ProductoRepository productoRepo,
                            ClienteRepository clienteRepo) {
        this.ventaRepo = ventaRepo;
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
    }

    @Override
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no existe"));

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());

        List<VentaDetalle> detalles = new ArrayList<>();
        double total = 0;

        for (VentaItemDTO item : dto.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new BusinessException("Producto no existe"));

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);

            VentaDetalle det = new VentaDetalle();
            det.setVenta(venta);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(producto.getPrecio());

            total += item.getCantidad() * producto.getPrecio();
            detalles.add(det);
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);

        Venta guardada = ventaRepo.save(venta);

        return new VentaResponseDTO(
                guardada.getId(),
                cliente.getId(),
                guardada.getFecha(),
                guardada.getTotal()
        );
    }

    @Override
    public List<VentaResponseDTO> listar() {
        return ventaRepo.findAll()
                .stream()
                .map(v -> new VentaResponseDTO(
                        v.getId(),
                        v.getCliente().getId(),
                        v.getFecha(),
                        v.getTotal()
                ))
                .toList();
    }

    @Override
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no existe"));

        return new VentaResponseDTO(
                venta.getId(),
                venta.getCliente().getId(),
                venta.getFecha(),
                venta.getTotal()
        );
    }
}
