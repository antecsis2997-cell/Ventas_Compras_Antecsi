package com.antecsis.service.impl;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.dto.inventario.AjusteStockRequestDTO;
import com.antecsis.dto.inventario.MovimientoResponseDTO;
import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.MovimientoInventarioRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.InventarioService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponseDTO> listarTodo(Pageable pageable, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        if (effectiveId != null) {
            return productoRepo.findBySectorId(effectiveId, pageable).map(this::toDTO);
        }
        return productoRepo.findAll(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventarioResponseDTO> stockBajo(Integer limite, Pageable pageable, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        if (effectiveId != null) {
            return productoRepo.findBySectorIdAndStockLessThanEqual(effectiveId, limite, pageable).map(this::toDTO);
        }
        return productoRepo.findByStockLessThanEqual(limite, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponseDTO> listarMovimientos(Pageable pageable, Long productoId) {
        Long sectorId = obtenerSectorIdAutenticado();
        Page<MovimientoInventario> page;
        if (productoId != null && sectorId != null) {
            page = movimientoRepo.findByProductoIdAndSectorId(productoId, sectorId, pageable);
        } else if (productoId != null) {
            page = movimientoRepo.findByProductoId(productoId, pageable);
        } else if (sectorId != null) {
            page = movimientoRepo.findBySectorId(sectorId, pageable);
        } else {
            page = movimientoRepo.findAll(pageable);
        }
        return page.map(this::toMovimientoDTO);
    }

    @Override
    @Transactional
    public MovimientoResponseDTO ajustarStock(AjusteStockRequestDTO dto) {
        Producto producto = productoRepo.findById(dto.getProductoId())
                .orElseThrow(() -> new BusinessException("Producto no existe"));
        Usuario usuario = obtenerUsuarioAutenticado();
        verificarAccesoSector(producto.getSector());

        int stockAnterior = producto.getStock();
        int nuevoStock = dto.getNuevoStock();
        int diferencia = nuevoStock - stockAnterior;

        if (diferencia == 0) {
            throw new BusinessException("El stock no cambió");
        }

        producto.setStock(nuevoStock);
        productoRepo.save(producto);

        registrarMovimiento(producto, TipoMovimiento.AJUSTE, Math.abs(diferencia),
                stockAnterior, nuevoStock,
                dto.getMotivo() != null ? dto.getMotivo() : "Ajuste manual",
                null, usuario, producto.getSector());

        return toMovimientoDTO(movimientoRepo.findByProductoId(producto.getId(),
                Pageable.ofSize(1)).getContent().get(0));
    }

    @Override
    @Transactional
    public void registrarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad,
                                    int stockAnterior, int stockNuevo, String motivo,
                                    Long referenciaId, Usuario usuario, Sector sector) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockNuevo(stockNuevo);
        mov.setMotivo(motivo);
        mov.setReferenciaId(referenciaId);
        mov.setUsuario(usuario);
        mov.setSector(sector);
        mov.setFecha(LocalDateTime.now());
        movimientoRepo.save(mov);
    }

    private InventarioResponseDTO toDTO(Producto p) {
        return new InventarioResponseDTO(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getMoneda(),
                p.getStock(),
                p.getUnidadMedida(),
                p.getStockMinimoAlerta(),
                p.getSector() != null ? p.getSector().getId() : null,
                p.getSector() != null ? p.getSector().getNombreSector() : null
        );
    }

    private MovimientoResponseDTO toMovimientoDTO(MovimientoInventario m) {
        return new MovimientoResponseDTO(
                m.getId(),
                m.getProducto().getId(),
                m.getProducto().getNombre(),
                m.getTipo().name(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockNuevo(),
                m.getMotivo(),
                m.getReferenciaId(),
                m.getUsuario() != null ? m.getUsuario().getUsername() : null,
                m.getFecha()
        );
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
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }
}
