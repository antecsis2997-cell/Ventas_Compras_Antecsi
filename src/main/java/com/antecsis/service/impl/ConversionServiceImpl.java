package com.antecsis.service.impl;

import com.antecsis.dto.conversion.ConversionRequestDTO;
import com.antecsis.dto.conversion.ConversionResponseDTO;
import com.antecsis.entity.*;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ConversionRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.RecetaRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.ConversionService;
import com.antecsis.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionServiceImpl implements ConversionService {

    private final RecetaRepository recetaRepo;
    private final ConversionRepository conversionRepo;
    private final ProductoRepository productoRepo;
    private final UsuarioRepository usuarioRepo;
    private final InventarioService inventarioService;

    @Override
    @Transactional
    public ConversionResponseDTO convertir(ConversionRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sectorUsuario = usuario.getSede();
        if (sectorUsuario == null) {
            throw new BusinessException("El usuario autenticado no tiene sector asignado");
        }

        Receta receta = recetaRepo.findById(dto.recetaId())
                .orElseThrow(() -> new BusinessException("Receta no existe"));
        verificarAccesoSector(receta.getSector());

        if (receta.getDetalles() == null || receta.getDetalles().isEmpty()) {
            throw new BusinessException("La receta no tiene insumos configurados");
        }

        Producto productoSalida = receta.getProductoSalida();
        if (productoSalida == null || Boolean.TRUE.equals(productoSalida.getEsInsumo())) {
            throw new BusinessException("La receta debe producir un PRODUCTO vendible (no insumo)");
        }

        Integer cantidadSalidaBase = receta.getCantidadSalidaBase();
        if (cantidadSalidaBase == null || cantidadSalidaBase <= 0) {
            throw new BusinessException("Receta inválida: cantidadSalidaBase debe ser > 0");
        }

        Integer cantidadProducir = dto.cantidadProducir();
        if (cantidadProducir == null || cantidadProducir <= 0) {
            throw new BusinessException("cantidadProducir debe ser > 0");
        }

        // Recomendación para tu caso: sin fracciones -> solo múltiplos enteros.
        if (cantidadProducir % cantidadSalidaBase != 0) {
            throw new BusinessException("La conversión solo permite cantidades múltiplos de la cantidadSalidaBase. " +
                    "cantidadProducir=" + cantidadProducir + ", cantidadSalidaBase=" + cantidadSalidaBase);
        }
        int scale = cantidadProducir / cantidadSalidaBase;

        // Crear conversión primero para obtener id de referencia en movimientos
        Conversion conversion = new Conversion();
        conversion.setReceta(receta);
        conversion.setSector(sectorUsuario);
        conversion.setUsuario(usuario);
        conversion.setCantidadProducir(cantidadProducir);
        conversion.setEstado(EstadoConversion.PENDIENTE);
        conversion.setFecha(LocalDateTime.now());
        conversion = conversionRepo.save(conversion);

        // 1) Consumir insumos
        for (RecetaDetalle det : receta.getDetalles()) {
            Producto insumo = det.getInsumo();
            if (insumo == null || !Boolean.TRUE.equals(insumo.getEsInsumo())) {
                throw new BusinessException("La receta contiene un insumo inválido (esInsumo debe ser true)");
            }
            int cantidadInsumoBase = det.getCantidadInsumoBase() != null ? det.getCantidadInsumoBase() : 0;
            if (cantidadInsumoBase <= 0) {
                throw new BusinessException("La receta contiene insumo con cantidadInsumoBase inválida");
            }

            int cantidadConsumida = cantidadInsumoBase * scale;
            int stockAnterior = insumo.getStock() != null ? insumo.getStock() : 0;
            if (stockAnterior < cantidadConsumida) {
                throw new BusinessException("Stock insuficiente del insumo '" + insumo.getNombre() +
                        "'. Disponible=" + stockAnterior + ", requerido=" + cantidadConsumida);
            }

            insumo.setStock(stockAnterior - cantidadConsumida);
            productoRepo.save(insumo);

            inventarioService.registrarMovimiento(
                    insumo,
                    TipoMovimiento.CONVERSION,
                    cantidadConsumida,
                    stockAnterior,
                    insumo.getStock(),
                    "Consumo insumo en conversión #" + conversion.getId(),
                    conversion.getId(),
                    usuario,
                    sectorUsuario
            );
        }

        // 2) Producir producto de salida
        int stockAnteriorSalida = productoSalida.getStock() != null ? productoSalida.getStock() : 0;
        int stockNuevoSalida = stockAnteriorSalida + cantidadProducir;
        productoSalida.setStock(stockNuevoSalida);
        productoRepo.save(productoSalida);

        inventarioService.registrarMovimiento(
                productoSalida,
                TipoMovimiento.CONVERSION,
                cantidadProducir,
                stockAnteriorSalida,
                stockNuevoSalida,
                "Producción producto en conversión #" + conversion.getId(),
                conversion.getId(),
                usuario,
                sectorUsuario
        );

        conversion.setEstado(EstadoConversion.COMPLETADA);
        conversion = conversionRepo.save(conversion);

        return new ConversionResponseDTO(
                conversion.getId(),
                receta.getId(),
                conversion.getCantidadProducir(),
                productoSalida.getNombre(),
                conversion.getFecha(),
                conversion.getEstado().name()
        );
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sectorUsuario = usuario.getSede();
        if (sectorUsuario != null && sectorEntidad != null && !sectorUsuario.getId().equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }
}

