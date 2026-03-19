package com.antecsis.service.impl;

import com.antecsis.dto.receta.RecetaDetalleResponseDTO;
import com.antecsis.dto.receta.RecetaRequestDTO;
import com.antecsis.dto.receta.RecetaResponseDTO;
import com.antecsis.dto.receta.RecetaDetalleRequestDTO;
import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.RecetaRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.RecetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecetaServiceImpl implements RecetaService {

    private final RecetaRepository recetaRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;

    @Override
    @Transactional
    public RecetaResponseDTO crear(RecetaRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sectorUsuario = usuario.getSede();
        if (sectorUsuario == null) {
            throw new BusinessException("El usuario autenticado no tiene sector (bodega) asignado");
        }

        Producto productoSalida = productoRepo.findById(dto.productoSalidaId())
                .orElseThrow(() -> new BusinessException("Producto de salida no existe"));
        verificarAccesoSector(productoSalida.getSector());
        if (Boolean.TRUE.equals(productoSalida.getEsInsumo())) {
            throw new BusinessException("La receta debe producir un PRODUCTO vendible (no insumo)");
        }

        Receta receta = new Receta();
        receta.setSector(sectorUsuario);
        receta.setProductoSalida(productoSalida);
        receta.setCantidadSalidaBase(dto.cantidadSalidaBase());
        receta.setActivo(true);

        List<RecetaDetalle> detalles = new ArrayList<>();
        for (RecetaDetalleRequestDTO d : dto.detalles()) {
            Producto insumo = productoRepo.findById(d.insumoId())
                    .orElseThrow(() -> new BusinessException("Insumo no existe: ID " + d.insumoId()));
            verificarAccesoSector(insumo.getSector());
            if (!Boolean.TRUE.equals(insumo.getEsInsumo())) {
                throw new BusinessException("Todos los insumos de la receta deben tener esInsumo=true");
            }
            RecetaDetalle det = new RecetaDetalle();
            det.setReceta(receta);
            det.setInsumo(insumo);
            det.setCantidadInsumoBase(d.cantidadInsumoBase());
            detalles.add(det);
        }

        receta.setDetalles(detalles);
        Receta guardada = recetaRepo.save(receta);

        log.info("Receta #{} creada. salida={}, cantidadSalidaBase={}",
                guardada.getId(), productoSalida.getNombre(), dto.cantidadSalidaBase());

        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecetaResponseDTO> listar(Pageable pageable) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sector = usuario.getSede();
        if (sector == null) {
            throw new BusinessException("El usuario autenticado no tiene sector (bodega) asignado");
        }
        return recetaRepo.findBySectorId(sector.getId(), pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RecetaResponseDTO obtenerPorId(Long id) {
        Receta receta = recetaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Receta no existe"));
        verificarAccesoSector(receta.getSector());
        return toResponseDTO(receta);
    }

    private RecetaResponseDTO toResponseDTO(Receta r) {
        List<RecetaDetalleResponseDTO> dets = r.getDetalles() == null ? List.of() :
                r.getDetalles().stream()
                        .map(d -> new RecetaDetalleResponseDTO(
                                d.getInsumo() != null ? d.getInsumo().getId() : null,
                                d.getInsumo() != null ? d.getInsumo().getNombre() : null,
                                d.getCantidadInsumoBase()
                        ))
                        .toList();

        return new RecetaResponseDTO(
                r.getId(),
                r.getSector() != null ? r.getSector().getId() : null,
                r.getProductoSalida() != null ? r.getProductoSalida().getId() : null,
                r.getProductoSalida() != null ? r.getProductoSalida().getNombre() : null,
                r.getCantidadSalidaBase(),
                dets,
                r.getActivo()
        );
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sectorUsuario = usuario.getSede();
        if (sectorUsuario != null && sectorEntidad != null && !sectorUsuario.getId().equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }
}

