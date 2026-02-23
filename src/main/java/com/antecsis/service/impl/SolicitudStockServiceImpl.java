package com.antecsis.service.impl;

import com.antecsis.dto.solicitudstock.SolicitudStockRequestDTO;
import com.antecsis.dto.solicitudstock.SolicitudStockResponseDTO;
import com.antecsis.entity.EstadoSolicitudStock;
import com.antecsis.entity.Producto;
import com.antecsis.entity.SolicitudStock;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.SolicitudStockRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.SolicitudStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolicitudStockServiceImpl implements SolicitudStockService {

    private final SolicitudStockRepository repository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public SolicitudStockResponseDTO crear(SolicitudStockRequestDTO dto) {
        Usuario solicitante = obtenerUsuarioActual();
        SolicitudStock s = new SolicitudStock();
        s.setSolicitante(solicitante);
        s.setNombre(solicitante.getNombre() != null ? solicitante.getNombre() : solicitante.getUsername());
        s.setApellidos(solicitante.getApellido() != null ? solicitante.getApellido() : "");
        s.setCargo(solicitante.getRol() != null ? solicitante.getRol().getNombre() : "CAJERO");
        s.setAsunto(dto.getAsunto());
        s.setRemitenteEmail(dto.getRemitenteEmail());
        s.setNombreRemitente(dto.getNombreRemitente());
        s.setUnidadMedida(dto.getUnidadMedida() != null ? dto.getUnidadMedida() : "UND");
        s.setCantidad(dto.getCantidad());
        s.setEstado(EstadoSolicitudStock.PENDIENTE);
        if (dto.getProductoId() != null) {
            Producto p = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new BusinessException("Producto no encontrado"));
            s.setProducto(p);
        }
        SolicitudStock guardado = repository.save(s);
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudStockResponseDTO> listar(Pageable pageable) {
        return repository.findByOrderByFechaCreacionDesc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional
    public SolicitudStockResponseDTO aprobar(Long id) {
        SolicitudStock s = repository.findById(id).orElseThrow(() -> new BusinessException("Solicitud no encontrada"));
        if (s.getEstado() != EstadoSolicitudStock.PENDIENTE) {
            throw new BusinessException("Solo se puede aprobar solicitudes pendientes");
        }
        s.setEstado(EstadoSolicitudStock.APROBADO);
        return toDTO(repository.save(s));
        // TODO: enviar mensaje/notificación al remitente "SU SOLICITUD ES APROBADA"
    }

    @Override
    @Transactional
    public SolicitudStockResponseDTO desaprobar(Long id) {
        SolicitudStock s = repository.findById(id).orElseThrow(() -> new BusinessException("Solicitud no encontrada"));
        if (s.getEstado() != EstadoSolicitudStock.PENDIENTE) {
            throw new BusinessException("Solo se puede desaprobar solicitudes pendientes");
        }
        s.setEstado(EstadoSolicitudStock.DESAPROBADO);
        return toDTO(repository.save(s));
        // TODO: enviar mensaje/notificación al remitente "SU SOLICITUD HA SIDO DESAPROBADA"
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private SolicitudStockResponseDTO toDTO(SolicitudStock s) {
        return new SolicitudStockResponseDTO(
                s.getId(),
                s.getNombre(),
                s.getApellidos(),
                s.getCargo(),
                s.getAsunto(),
                s.getRemitenteEmail(),
                s.getNombreRemitente(),
                s.getProducto() != null ? s.getProducto().getId() : null,
                s.getProducto() != null ? s.getProducto().getNombre() : null,
                s.getUnidadMedida(),
                s.getCantidad(),
                s.getEstado().name(),
                s.getFechaCreacion()
        );
    }
}
