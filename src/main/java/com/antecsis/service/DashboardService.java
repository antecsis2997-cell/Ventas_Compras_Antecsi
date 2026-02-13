package com.antecsis.service;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;

import java.time.LocalDate;

public interface DashboardService {
    DashboardVentasDTO ventasPorDia(LocalDate dia);
    DashboardVentasDTO ventasPorMes(int year, int month);
    DashboardVentasDTO ventasPorAnio(int year);
    ProductoMasVendidoDTO productoMasVendido();

    /** Pedidos facturados (COMPLETADA) y anulados (ANULADA) en el mes. */
    DashboardPedidosEstadoDTO pedidosFacturadosYAnuladosPorMes(int year, int month);
}
