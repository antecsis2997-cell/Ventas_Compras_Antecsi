package com.antecsis.service;

import com.antecsis.dto.DashboardPedidosEstadoDTO;
import com.antecsis.dto.DashboardPedidosPendientesDeliveryDTO;
import com.antecsis.dto.DashboardVentasDTO;
import com.antecsis.dto.producto.ProductoMasVendidoDTO;

import java.time.LocalDate;

public interface DashboardService {
    DashboardVentasDTO ventasPorDia(LocalDate dia, Long sectorId);
    DashboardVentasDTO ventasPorMes(int year, int month, Long sectorId);
    DashboardVentasDTO ventasPorAnio(int year, Long sectorId);
    ProductoMasVendidoDTO productoMasVendido(Long sectorId);

    DashboardPedidosEstadoDTO pedidosFacturadosYAnuladosPorMes(int year, int month, Long sectorId);

    /**
     * Informe de órdenes (ventas) que requieren delivery y aún no están entregadas,
     * agrupadas por estadoEntrega.
     */
    DashboardPedidosPendientesDeliveryDTO pedidosPendientesDelivery(Long sectorId);
}
