package com.antecsis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.antecsis.dto.InventarioResponseDTO;
import com.antecsis.dto.inventario.AjusteStockRequestDTO;
import com.antecsis.dto.inventario.MovimientoResponseDTO;
import com.antecsis.entity.Producto;
import com.antecsis.entity.Sector;
import com.antecsis.entity.TipoMovimiento;
import com.antecsis.entity.Usuario;

public interface InventarioService {
    Page<InventarioResponseDTO> listarTodo(Pageable pageable, Long sectorId);
    Page<InventarioResponseDTO> stockBajo(Integer limite, Pageable pageable, Long sectorId);
    Page<InventarioResponseDTO> stockBajoPorAlerta(Pageable pageable, Long sectorId);
    Page<InventarioResponseDTO> listarInsumosTodo(Pageable pageable, Long sectorId);
    Page<InventarioResponseDTO> stockBajoInsumos(Integer limite, Pageable pageable, Long sectorId);
    Page<MovimientoResponseDTO> listarMovimientos(Pageable pageable, Long productoId);
    MovimientoResponseDTO ajustarStock(AjusteStockRequestDTO dto);
    void registrarMovimiento(Producto producto, TipoMovimiento tipo, int cantidad,
                             int stockAnterior, int stockNuevo, String motivo,
                             Long referenciaId, Usuario usuario, Sector sector);
}
