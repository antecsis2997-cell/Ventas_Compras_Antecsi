package com.antecsis.service.impl;

import com.antecsis.dto.compra.CompraItemDTO;
import com.antecsis.dto.compra.CompraRequestDTO;
import com.antecsis.dto.compra.CompraResponseDTO;
import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.*;
import com.antecsis.service.CompraService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepo;
    private final ProductoRepository productoRepo;
    private final ProveedorRepository proveedorRepo;
    private final UsuarioRepository usuarioRepo;
    private final MetodoPagoRepository metodoPagoRepo;

    @Override
    @Transactional
    public CompraResponseDTO crear(CompraRequestDTO dto) {
        var proveedor = proveedorRepo.findById(dto.getProveedorId())
                .orElseThrow(() -> new BusinessException("Proveedor no existe"));

        Usuario usuario = obtenerUsuarioAutenticado();

        Compra compra = new Compra();
        compra.setProveedor(proveedor);
        compra.setUsuario(usuario);
        compra.setFecha(LocalDateTime.now());
        compra.setEstado(EstadoCompra.COMPLETADA);
        compra.setObservaciones(dto.getObservaciones());
        compra.setNumeroDocumento(dto.getNumeroDocumento());

        if (dto.getMetodoPagoId() != null) {
            MetodoPago mp = metodoPagoRepo.findById(dto.getMetodoPagoId())
                    .orElseThrow(() -> new BusinessException("Método de pago no existe"));
            compra.setMetodoPago(mp);
        }

        List<CompraDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CompraItemDTO item : dto.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new BusinessException("Producto no existe: ID " + item.getProductoId()));

            producto.setStock(producto.getStock() + item.getCantidad());
            productoRepo.save(producto);

            CompraDetalle det = new CompraDetalle();
            det.setCompra(compra);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(item.getPrecioUnitario());

            BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
            detalles.add(det);
        }

        compra.setTotal(total);
        compra.setDetalles(detalles);

        Compra guardada = compraRepo.save(compra);
        log.info("Compra #{} creada por {} - Total: {} - Proveedor: {}",
                guardada.getId(), usuario.getUsername(), total, proveedor.getNombre());

        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompraResponseDTO> listar(Pageable pageable) {
        return compraRepo.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponseDTO obtenerPorId(Long id) {
        Compra c = compraRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Compra no existe"));
        return toResponseDTO(c);
    }

    @Override
    @Transactional
    public CompraResponseDTO anular(Long id) {
        Compra compra = compraRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Compra no existe"));

        if (compra.getEstado() == EstadoCompra.ANULADA) {
            throw new BusinessException("La compra ya está anulada");
        }

        // Revertir stock
        for (CompraDetalle detalle : compra.getDetalles()) {
            Producto producto = detalle.getProducto();
            int nuevoStock = producto.getStock() - detalle.getCantidad();
            if (nuevoStock < 0) {
                throw new BusinessException("No se puede anular: el producto '"
                        + producto.getNombre() + "' ya vendió unidades del stock ingresado");
            }
            producto.setStock(nuevoStock);
            productoRepo.save(producto);
        }

        compra.setEstado(EstadoCompra.ANULADA);
        Compra guardada = compraRepo.save(compra);

        log.info("Compra #{} anulada por {}", id, obtenerUsuarioAutenticado().getUsername());
        return toResponseDTO(guardada);
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    private CompraResponseDTO toResponseDTO(Compra c) {
        return new CompraResponseDTO(
                c.getId(),
                c.getProveedor().getId(),
                c.getProveedor().getNombre(),
                c.getUsuario().getUsername(),
                c.getMetodoPago() != null ? c.getMetodoPago().getNombre() : null,
                c.getFecha(),
                c.getTotal(),
                c.getEstado().name(),
                c.getObservaciones(),
                c.getNumeroDocumento()
        );
    }
}
