package com.antecsis.service.impl;

import com.antecsis.dto.historial.HistorialPedidoResponseDTO;
import com.antecsis.entity.HistorialPedido;
import com.antecsis.repository.HistorialPedidoRepository;
import com.antecsis.service.HistorialPedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialPedidoServiceImpl implements HistorialPedidoService {

    private final HistorialPedidoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialPedidoResponseDTO> listar(Pageable pageable) {
        return repository.findByOrderByFechaDesc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialPedidoResponseDTO> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        return repository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private HistorialPedidoResponseDTO toDTO(HistorialPedido h) {
        return new HistorialPedidoResponseDTO(
                h.getId(),
                h.getVenta().getId(),
                h.getProducto().getId(),
                h.getNombreProducto(),
                h.getCantidad(),
                h.getPrecioUnitario(),
                h.getSubtotal(),
                h.getFecha()
        );
    }
}
