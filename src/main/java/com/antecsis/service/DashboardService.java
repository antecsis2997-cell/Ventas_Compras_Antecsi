package com.antecsis.service;

import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;

import java.time.LocalDate;

public interface DashboardService {
    DashboardVentasDTO ventasPorDia(LocalDate dia);
    DashboardVentasDTO ventasPorMes(int year, int month);
    ProductoMasVendidoDTO productoMasVendido();
}
