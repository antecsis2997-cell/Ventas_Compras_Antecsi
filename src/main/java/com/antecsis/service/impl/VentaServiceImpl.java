package com.antecsis.service.impl;

import com.antecsis.dto.venta.VentaItemDTO;
import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.*;
import com.antecsis.service.VentaService;

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
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final HistorialPedidoRepository historialPedidoRepo;

    @Override
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente no existe"));

        Usuario usuario = obtenerUsuarioAutenticado();

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setObservaciones(dto.getObservaciones());
        if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().isBlank()) {
            try {
                venta.setTipoDocumento(TipoDocumentoVenta.valueOf(dto.getTipoDocumento().toUpperCase().trim()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Tipo de documento inválido. Use FACTURA o BOLETA.");
            }
        }
        venta.setNumeroDocumento(dto.getNumeroDocumento());

        if (dto.getMetodoPagoId() != null) {
            MetodoPago mp = metodoPagoRepo.findById(dto.getMetodoPagoId())
                    .orElseThrow(() -> new BusinessException("Método de pago no existe"));
            venta.setMetodoPago(mp);
        }

        List<VentaDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (VentaItemDTO item : dto.getItems()) {
            Producto producto = productoRepo.findById(item.getProductoId())
                    .orElseThrow(() -> new BusinessException("Producto no existe: ID " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepo.save(producto);

            VentaDetalle det = new VentaDetalle();
            det.setVenta(venta);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(producto.getPrecio());

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
            detalles.add(det);
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);

        Venta guardada = ventaRepo.save(venta);

        // Historial_Pedidos: carga rápida para reportes (documento: LOAD 1ms)
        LocalDateTime fechaVenta = guardada.getFecha();
        for (VentaDetalle det : guardada.getDetalles()) {
            HistorialPedido hp = new HistorialPedido();
            hp.setVenta(guardada);
            hp.setProducto(det.getProducto());
            hp.setNombreProducto(det.getProducto().getNombre());
            hp.setCantidad(det.getCantidad());
            hp.setPrecioUnitario(det.getPrecioUnitario());
            hp.setSubtotal(det.getPrecioUnitario().multiply(BigDecimal.valueOf(det.getCantidad())));
            hp.setFecha(fechaVenta);
            historialPedidoRepo.save(hp);
        }

        log.info("Venta #{} creada por {} - Total: {} - Cliente: {}",
                guardada.getId(), usuario.getUsername(), total, cliente.getNombre());

        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> listar(Pageable pageable) {
        return ventaRepo.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        return toResponseDTO(venta);
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta ya está anulada");
        }

        // Devolver stock
        for (VentaDetalle detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepo.save(producto);
        }

        venta.setEstado(EstadoVenta.ANULADA);
        Venta guardada = ventaRepo.save(venta);

        log.info("Venta #{} anulada por {}", id, obtenerUsuarioAutenticado().getUsername());
        return toResponseDTO(guardada);
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    private VentaResponseDTO toResponseDTO(Venta v) {
        return new VentaResponseDTO(
                v.getId(),
                v.getCliente().getId(),
                v.getCliente().getNombre(),
                v.getUsuario().getUsername(),
                v.getMetodoPago() != null ? v.getMetodoPago().getNombre() : null,
                v.getFecha(),
                v.getTotal(),
                v.getEstado().name(),
                v.getTipoDocumento() != null ? v.getTipoDocumento().name() : null,
                v.getNumeroDocumento(),
                v.getObservaciones()
        );
    }
}
