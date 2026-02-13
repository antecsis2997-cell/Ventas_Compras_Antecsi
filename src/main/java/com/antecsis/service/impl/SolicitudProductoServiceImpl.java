package com.antecsis.service.impl;

import com.antecsis.dto.solicitud.SolicitudProductoRequestDTO;
import com.antecsis.dto.solicitud.SolicitudProductoResponseDTO;
import com.antecsis.entity.Producto;
import com.antecsis.entity.SolicitudProducto;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.SolicitudProductoRepository;
import com.antecsis.service.SolicitudProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudProductoServiceImpl implements SolicitudProductoService {

    private final SolicitudProductoRepository repository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public SolicitudProductoResponseDTO crear(SolicitudProductoRequestDTO dto) {
        SolicitudProducto s = new SolicitudProducto();
        s.setNombreEmisor(dto.getNombreEmisor());
        s.setDescripcion(dto.getDescripcion());
        s.setPrecio(dto.getPrecio());
        s.setEstado(dto.getEstado() != null ? dto.getEstado() : com.antecsis.entity.EstadoSolicitud.LEVE);
        if (dto.getProductoId() != null) {
            Producto p = productoRepository.findById(dto.getProductoId()).orElse(null);
            s.setProducto(p);
        }
        SolicitudProducto guardado = repository.save(s);
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudProductoResponseDTO> listar(Pageable pageable) {
        return repository.findByOrderByFechaDesc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudProductoResponseDTO> listarPendientes() {
        return repository.findByAtendidaFalseOrderByFechaDesc().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudProductoResponseDTO obtenerPorId(Long id) {
        SolicitudProducto s = repository.findById(id).orElseThrow(() -> new BusinessException("Solicitud no existe"));
        return toDTO(s);
    }

    @Override
    @Transactional
    public SolicitudProductoResponseDTO marcarAtendida(Long id) {
        SolicitudProducto s = repository.findById(id).orElseThrow(() -> new BusinessException("Solicitud no existe"));
        s.setAtendida(true);
        return toDTO(repository.save(s));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) throw new BusinessException("Solicitud no existe");
        repository.deleteById(id);
    }

    private SolicitudProductoResponseDTO toDTO(SolicitudProducto s) {
        return new SolicitudProductoResponseDTO(
                s.getId(),
                s.getNombreEmisor(),
                s.getProducto() != null ? s.getProducto().getId() : null,
                s.getProducto() != null ? s.getProducto().getNombre() : null,
                s.getDescripcion(),
                s.getPrecio(),
                s.getEstado().name(),
                s.getFecha(),
                s.getAtendida()
        );
    }
}
