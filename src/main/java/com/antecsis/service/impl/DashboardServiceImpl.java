package com.antecsis.service.impl;

import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.VentaDetalleRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepo;
    private final VentaDetalleRepository ventaDetalleRepo;

    public DashboardServiceImpl(VentaRepository ventaRepo, VentaDetalleRepository ventaDetalleRepo) {
        this.ventaRepo = ventaRepo;
        this.ventaDetalleRepo = ventaDetalleRepo;
    }

    @Override
    public DashboardVentasDTO ventasPorDia(LocalDate dia) {
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(LocalTime.MAX);

        var ventas = ventaRepo.findByFechaBetween(inicio, fin);

        double total = ventas.stream()
                .mapToDouble(v -> v.getTotal())
                .sum();

        return new DashboardVentasDTO(
                (long) ventas.size(),
                total
        );
    }

    @Override
    public DashboardVentasDTO ventasPorMes(int year, int month) {
        LocalDate inicioMes = LocalDate.of(year, month, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        var ventas = ventaRepo.findByFechaBetween(inicio, fin);

        double total = ventas.stream()
                .mapToDouble(v -> v.getTotal())
                .sum();

        return new DashboardVentasDTO(
                (long) ventas.size(),
                total
        );
    }

	@Override
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


}
