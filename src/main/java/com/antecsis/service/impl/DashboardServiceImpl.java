package com.antecsis.service.impl;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.entity.EstadoVenta;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.VentaDetalleRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepo;
    private final VentaDetalleRepository ventaDetalleRepo;

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorDia(LocalDate dia) {
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(LocalTime.MAX);

        var ventas = ventaRepo.findByFechaBetween(inicio, fin);

        BigDecimal total = ventas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorMes(int year, int month) {
        LocalDate inicioMes = LocalDate.of(year, month, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        var ventas = ventaRepo.findByFechaBetween(inicio, fin);

        BigDecimal total = ventas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardVentasDTO ventasPorAnio(int year) {
        LocalDateTime inicio = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
        var ventas = ventaRepo.findByFechaBetween(inicio, fin);
        BigDecimal total = ventas.stream()
                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardVentasDTO((long) ventas.size(), total);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoMasVendidoDTO productoMasVendido() {
        List<Object[]> resultado = ventaDetalleRepo.productoMasVendido();

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
    public DashboardPedidosEstadoDTO pedidosFacturadosYAnuladosPorMes(int year, int month) {
        LocalDate inicioMes = LocalDate.of(year, month, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);
        long facturados = ventaRepo.countByEstadoAndFechaBetween(EstadoVenta.COMPLETADA, inicio, fin);
        long anulados = ventaRepo.countByEstadoAndFechaBetween(EstadoVenta.ANULADA, inicio, fin);
        return new DashboardPedidosEstadoDTO(facturados, anulados);
    }
}
