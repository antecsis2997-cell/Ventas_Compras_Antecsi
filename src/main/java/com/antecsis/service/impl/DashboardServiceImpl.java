package com.antecsis.service.impl;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardPedidosPendientesDeliveryDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.entity.EstadoEntrega;
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
import java.util.ArrayList;
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
        long totalVentas = ventas.size();
        long completadas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.COMPLETADA).count();
        long anuladas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.ANULADA).count();
        long pendientes = totalVentas - completadas - anuladas;

        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            labels.add(String.format("%02d:00", h));
            valores.add(0.0);
        }
        double[] buckets = new double[24];
        for (var v : ventas) {
            int hour = v.getFecha() != null ? v.getFecha().getHour() : 0;
            buckets[hour] += Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO).doubleValue();
        }
        for (int h = 0; h < 24; h++) {
            valores.set(h, buckets[h]);
        }

        return new DashboardVentasDTO(totalVentas, total, labels, valores, completadas, anuladas, pendientes);
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
        long totalVentas = ventas.size();
        long completadas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.COMPLETADA).count();
        long anuladas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.ANULADA).count();
        long pendientes = totalVentas - completadas - anuladas;

        int diasMes = inicioMes.lengthOfMonth();
        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        for (int d = 1; d <= diasMes; d++) {
            labels.add(String.valueOf(d));
            valores.add(0.0);
        }

        double[] buckets = new double[diasMes];
        for (var v : ventas) {
            if (v.getFecha() == null) continue;
            int dayIndex = v.getFecha().getDayOfMonth() - 1;
            if (dayIndex >= 0 && dayIndex < diasMes) {
                buckets[dayIndex] += Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO).doubleValue();
            }
        }
        for (int d = 0; d < diasMes; d++) {
            valores.set(d, buckets[d]);
        }

        return new DashboardVentasDTO(totalVentas, total, labels, valores, completadas, anuladas, pendientes);
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
        long totalVentas = ventas.size();
        long completadas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.COMPLETADA).count();
        long anuladas = ventas.stream().filter(v -> v.getEstado() == EstadoVenta.ANULADA).count();
        long pendientes = totalVentas - completadas - anuladas;

        List<String> labels = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        String[] meses = new String[]{"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        for (int m = 1; m <= 12; m++) {
            labels.add(meses[m - 1]);
            valores.add(0.0);
        }

        double[] buckets = new double[12];
        for (var v : ventas) {
            if (v.getFecha() == null) continue;
            int monthIndex = v.getFecha().getMonthValue() - 1;
            if (monthIndex >= 0 && monthIndex < 12) {
                buckets[monthIndex] += Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO).doubleValue();
            }
        }
        for (int m = 0; m < 12; m++) {
            valores.set(m, buckets[m]);
        }

        return new DashboardVentasDTO(totalVentas, total, labels, valores, completadas, anuladas, pendientes);
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

    @Override
    @Transactional(readOnly = true)
    public DashboardPedidosPendientesDeliveryDTO pedidosPendientesDelivery(Long sectorId) {
        Long effectiveId = resolverSectorId(sectorId);

        long totalRequiereDelivery = ventaRepo.countRequiereDeliveryByEstadoVenta(effectiveId, EstadoVenta.PENDIENTE);
        long pendientes = ventaRepo.countRequiereDeliveryByEstadoEntrega(effectiveId, EstadoVenta.PENDIENTE, EstadoEntrega.PENDIENTE);
        long enCamino = ventaRepo.countRequiereDeliveryByEstadoEntrega(effectiveId, EstadoVenta.PENDIENTE, EstadoEntrega.EN_CAMINO);
        long entregados = ventaRepo.countRequiereDeliveryByEstadoEntrega(effectiveId, EstadoVenta.COMPLETADA, EstadoEntrega.ENTREGADO);

        return new DashboardPedidosPendientesDeliveryDTO(totalRequiereDelivery, pendientes, enCamino, entregados);
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
