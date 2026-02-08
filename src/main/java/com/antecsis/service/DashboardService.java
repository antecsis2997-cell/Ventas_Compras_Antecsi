package com.antecsis.service;

import com.antecsis.dto.DashboardVentasDTO;

import java.time.LocalDate;

public interface DashboardService {
    DashboardVentasDTO ventasPorDia(LocalDate dia);
    DashboardVentasDTO ventasPorMes(int year, int month);
}
