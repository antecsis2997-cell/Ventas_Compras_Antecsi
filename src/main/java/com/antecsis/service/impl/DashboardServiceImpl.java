package com.antecsis.service.impl;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.entity.EstadoVenta;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.repository.VentaDetalleRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepo;
    private final VentaDetalleRepository ventaDetalleRepo;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorDia(LocalDate dia, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(LocalTime.MAX);

        var ventas = effectiveId != null
                ? ventaRepo.findByFechaBetweenAndSectorId(inicio, fin, effectiveId)
                : ventaRepo.findByFechaBetween(inicio, fin);

        BigDecimal total = ventas.stream()
                .map(v -> Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorMes(int year, int month, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDate inicioMes = LocalDate.of(year, month, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        var ventas = effectiveId != null
                ? ventaRepo.findByFechaBetweenAndSectorId(inicio, fin, effectiveId)
                : ventaRepo.findByFechaBetween(inicio, fin);

        BigDecimal total = ventas.stream()
                .map(v -> Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorAnio(int year, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDateTime inicio = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
        var ventas = effectiveId != null
                ? ventaRepo.findByFechaBetweenAndSectorId(inicio, fin, effectiveId)
                : ventaRepo.findByFechaBetween(inicio, fin);
        BigDecimal total = ventas.stream()
                .map(v -> Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoMasVendidoDTO productoMasVendido(Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);

        List<Object[]> resultado = effectiveId != null
                ? ventaDetalleRepo.productoMasVendidoBySectorId(effectiveId)
                : ventaDetalleRepo.productoMasVendido();

        if (resultado.isEmpty()) {
            throw new BusinessException("No existen ventas registradas");
        }

        Object[] fila = resultado.get(0);

        return new ProductoMasVendidoDTO(
                (Long) fila[0],
                (String) fila[1],
                (Long) fila[2]
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardPedidosEstadoDTO pedidosFacturadosYAnuladosPorMes(int year, int month, Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);
        LocalDate inicioMes = LocalDate.of(year, month, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);
        long facturados;
        long anulados;
        if (effectiveId != null) {
            facturados = ventaRepo.countByEstadoAndFechaBetweenAndSectorId(EstadoVenta.COMPLETADA, inicio, fin, effectiveId);
            anulados = ventaRepo.countByEstadoAndFechaBetweenAndSectorId(EstadoVenta.ANULADA, inicio, fin, effectiveId);
        } else {
            facturados = ventaRepo.countByEstadoAndFechaBetween(EstadoVenta.COMPLETADA, inicio, fin);
            anulados = ventaRepo.countByEstadoAndFechaBetween(EstadoVenta.ANULADA, inicio, fin);
        }
        return new DashboardPedidosEstadoDTO(facturados, anulados);
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
