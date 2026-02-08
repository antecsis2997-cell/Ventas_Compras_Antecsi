package com.antecsis.service.impl;

import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.repository.VentaRepository;
import com.antecsis.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final VentaRepository ventaRepo;

    public DashboardServiceImpl(VentaRepository ventaRepo) {
        this.ventaRepo = ventaRepo;
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


}
