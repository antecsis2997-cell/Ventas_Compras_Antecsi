package com.antecsis.service.impl;

import com.antecsis.dto.venta.VentaItemDTO;
import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.entity.*;
import com.antecsis.entity.TipoMovimiento;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.*;
import com.antecsis.service.InventarioService;
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
    private final InventarioService inventarioService;

    @Override
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        Cliente cliente = clienteRepo.findById(dto.getClienteId())
                .orElseThrow(() -> new BusinessException("Cliente no existe"));

        Usuario usuario = obtenerUsuarioAutenticado();
        verificarAccesoSector(cliente.getSector());

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setSector(usuario.getSede());
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setObservaciones(dto.getObservaciones());
        venta.setMoneda(dto.getMoneda() != null ? dto.getMoneda() : "PEN");
        venta.setConCuotas(dto.getConCuotas());
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
            verificarAccesoSector(producto.getSector());

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - item.getCantidad());
            productoRepo.save(producto);

            inventarioService.registrarMovimiento(producto, TipoMovimiento.VENTA,
                    item.getCantidad(), stockAnterior, producto.getStock(),
                    "Venta", null, usuario, usuario.getSede());

            VentaDetalle det = new VentaDetalle();
            det.setVenta(venta);
            det.setProducto(producto);
            det.setCantidad(item.getCantidad());
            
            // Usar precio personalizado si viene en el DTO, sino usar el del catálogo
            BigDecimal precioUnitario = item.getPrecioUnitario() != null 
                    ? item.getPrecioUnitario() 
                    : producto.getPrecio();
            det.setPrecioUnitario(precioUnitario);

            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);
            detalles.add(det);
        }

        venta.setTotal(total);
        venta.setDetalles(detalles);

        Venta guardada = ventaRepo.save(venta);

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
            hp.setSector(guardada.getSector());
            historialPedidoRepo.save(hp);
        }

        log.info("Venta #{} creada por {} - Total: {} - Cliente: {}",
                guardada.getId(), usuario.getUsername(), total, cliente.getNombre());

        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> listar(Pageable pageable, Long sectorId) {
        Long effectiveSectorId = resolverSectorId(sectorId);
        if (effectiveSectorId != null) {
            return ventaRepo.findBySectorId(effectiveSectorId, pageable).map(this::toResponseDTO);
        }
        return ventaRepo.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());
        return toResponseDTO(venta);
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta ya está anulada");
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        for (VentaDetalle detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior + detalle.getCantidad());
            productoRepo.save(producto);

            inventarioService.registrarMovimiento(producto, TipoMovimiento.ANULACION_VENTA,
                    detalle.getCantidad(), stockAnterior, producto.getStock(),
                    "Anulación venta #" + id, id, usuario, venta.getSector());
        }

        venta.setEstado(EstadoVenta.ANULADA);
        Venta guardada = ventaRepo.save(venta);

        log.info("Venta #{} anulada por {}", id, usuario.getUsername());
        return toResponseDTO(guardada);
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdUsuario != null && sectorEntidad != null
                && !sectorIdUsuario.equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
    }

    private Long resolverSectorId(Long sectorIdParam) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdParam != null) {
            if (sectorIdUsuario != null && !sectorIdUsuario.equals(sectorIdParam)) {
                throw new BusinessException("No tiene acceso a este sector");
            }
            return sectorIdParam;
        }
        return sectorIdUsuario;
    }

    private Long obtenerSectorIdAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return usuario.getSede() != null ? usuario.getSede().getId() : null;
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    private VentaResponseDTO toResponseDTO(Venta v) {
        List<VentaResponseDTO.VentaItemDTO> items = v.getDetalles().stream()
                .map(d -> new VentaResponseDTO.VentaItemDTO(
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                ))
                .toList();

        return new VentaResponseDTO(
                v.getId(),
                v.getCliente().getId(),
                v.getCliente().getNombre(),
                v.getUsuario().getUsername(),
                v.getSector() != null ? v.getSector().getId() : null,
                v.getSector() != null ? v.getSector().getNombreSector() : null,
                v.getMetodoPago() != null ? v.getMetodoPago().getNombre() : null,
                v.getFecha(),
                v.getTotal(),
                v.getEstado().name(),
                v.getTipoDocumento() != null ? v.getTipoDocumento().name() : null,
                v.getNumeroDocumento(),
                v.getObservaciones(),
                v.getMoneda(),
                v.getConCuotas(),
                items
        );
    }
}
