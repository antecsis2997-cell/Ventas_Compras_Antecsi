package com.antecsis.service.impl;

import com.antecsis.dto.venta.VentaItemDTO;
import com.antecsis.dto.venta.VentaRequestDTO;
import com.antecsis.dto.venta.VentaResponseDTO;
import com.antecsis.entity.Cliente;
import com.antecsis.entity.EstadoEntrega;
import com.antecsis.entity.EstadoVenta;
import com.antecsis.entity.HistorialPedido;
import com.antecsis.entity.MetodoPago;
import com.antecsis.entity.Producto;
import com.antecsis.entity.Sector;
import com.antecsis.entity.TipoDocumentoVenta;
import com.antecsis.entity.TipoEntrega;
import com.antecsis.entity.TipoMovimiento;
import com.antecsis.entity.Usuario;
import com.antecsis.entity.Venta;
import com.antecsis.entity.VentaDetalle;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ClienteRepository;
import com.antecsis.repository.HistorialPedidoRepository;
import com.antecsis.repository.MetodoPagoRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.repository.VentaRepository;
import com.antecsis.entity.ConfiguracionFiscal;
import com.antecsis.service.ConfiguracionFiscalService;
import com.antecsis.service.InventarioService;
import com.antecsis.service.SecuenciaComprobanteService;
import com.antecsis.service.VentaService;
import com.antecsis.service.sunat.SunatVentaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.antecsis.dto.logistica.MetricasEntregasVendedorDTO;
import com.antecsis.dto.logistica.LogisticaEntregaDetalleDTO;
import com.antecsis.dto.venta.ConfirmacionEntregaRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepo;
    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final MetodoPagoRepository metodoPagoRepo;
    private final HistorialPedidoRepository historialPedidoRepo;
    private final InventarioService inventarioService;
    private final SecuenciaComprobanteService secuenciaComprobanteService;
    private final ConfiguracionFiscalService configuracionFiscalService;
    private final SectorRepository sectorRepo;
    private final PayuPaymentService payuPaymentService;
    private final SunatVentaService sunatVentaService;

    @Override
    @Transactional(readOnly = true)
    public String siguienteNumeroComprobantePreview(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.isBlank()) return null;
        TipoDocumentoVenta tipo;
        try {
            tipo = TipoDocumentoVenta.valueOf(tipoDocumento.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
        Usuario usuario = obtenerUsuarioAutenticado();
        Sector sector = usuario.getSede();
        if (sector == null || sector.getId() == null) return null;
        sector = sectorRepo.findById(sector.getId()).orElse(sector);

        // La serie proviene exclusivamente de ConfiguracionFiscal (SEE del Contribuyente)
        Optional<ConfiguracionFiscal> cfgOpt = configuracionFiscalService.buscarActivaPorSector(sector.getId());
        if (cfgOpt.isPresent()) {
            ConfiguracionFiscal cfg = cfgOpt.get();
            String serie = tipo == TipoDocumentoVenta.BOLETA ? cfg.getSerieBoleta() : cfg.getSerieFactura();
            if (serie != null && !serie.isBlank()) {
                return secuenciaComprobanteService.siguienteNumeroPreviewConPrefijo(sector, tipo, serie);
            }
        }

        return null;
    }

    @Override
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        // clienteId es opcional: las boletas pueden emitirse como "Consumidor Final" sin identificar al comprador.
        // Las facturas SIEMPRE requieren cliente con RUC.
        boolean esFactura = dto.tipoDocumento() != null
                && "FACTURA".equalsIgnoreCase(dto.tipoDocumento().trim());

        if (esFactura && dto.clienteId() == null) {
            throw new BusinessException("La Factura requiere seleccionar un cliente con RUC.");
        }

        Cliente cliente = null;
        if (dto.clienteId() != null) {
            cliente = clienteRepo.findById(dto.clienteId())
                    .orElseThrow(() -> new BusinessException("Cliente no existe"));
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        if (cliente != null) {
            verificarAccesoSector(cliente.getSector());
        }

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setSector(usuario.getSede());
        venta.setFecha(LocalDateTime.now());
        venta.setObservaciones(dto.observaciones());
        venta.setMoneda(Objects.requireNonNullElse(dto.moneda(), "PEN"));
        venta.setConCuotas(dto.conCuotas());

        // Delivery: si requiere delivery, venta queda PENDIENTE hasta que Logística marque entregado
        Boolean requiereDelivery = Boolean.TRUE.equals(dto.requiereDelivery());
        venta.setRequiereDelivery(requiereDelivery);
        if (requiereDelivery) {
            venta.setEstado(EstadoVenta.PENDIENTE);
            venta.setEstadoEntrega(EstadoEntrega.PENDIENTE);
            if (dto.tipoEntrega() != null && !dto.tipoEntrega().isBlank()) {
                try {
                    venta.setTipoEntrega(TipoEntrega.valueOf(dto.tipoEntrega().toUpperCase().trim()));
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Tipo de entrega inválido. Use INMEDIATA, PROGRAMADA_3_5 o PROGRAMADA_5_6_MESES.");
                }
            }
            venta.setDireccionEntrega(dto.direccionEntrega());
            if (venta.getTipoEntrega() == TipoEntrega.INMEDIATA
                    && (venta.getDireccionEntrega() == null || venta.getDireccionEntrega().isBlank())) {
                throw new BusinessException("La dirección de entrega es obligatoria cuando el tipo es INMEDIATA.");
            }
            if (venta.getTipoEntrega() == TipoEntrega.PROGRAMADA_5_6_MESES
                    && (venta.getDireccionEntrega() == null || venta.getDireccionEntrega().isBlank())) {
                throw new BusinessException("La dirección de entrega es obligatoria para entregas de 5 a 6 meses.");
            }
        } else {
            venta.setEstado(EstadoVenta.COMPLETADA);
            venta.setTipoEntrega(null);
            venta.setDireccionEntrega(null);
            venta.setEstadoEntrega(null);
        }

        if (dto.tipoDocumento() != null && !dto.tipoDocumento().isBlank()) {
            try {
                venta.setTipoDocumento(TipoDocumentoVenta.valueOf(dto.tipoDocumento().toUpperCase().trim()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Tipo de documento inválido. Use FACTURA o BOLETA.");
            }
        }
        // Generar el número de comprobante.
        // Prioridad: serie de ConfiguracionFiscal SUNAT activa → prefijo del Sector → número manual del DTO.
        Sector sectorParaNumero = venta.getSector();
        if (sectorParaNumero != null && sectorParaNumero.getId() != null) {
            sectorParaNumero = sectorRepo.findById(sectorParaNumero.getId()).orElse(sectorParaNumero);
        }

        // La serie proviene exclusivamente de ConfiguracionFiscal activa (SEE del Contribuyente)
        String numeroGenerado = null;
        if (sectorParaNumero != null && venta.getTipoDocumento() != null) {
            Optional<ConfiguracionFiscal> cfgOpt = configuracionFiscalService.buscarActivaPorSector(sectorParaNumero.getId());
            if (cfgOpt.isPresent()) {
                ConfiguracionFiscal cfg = cfgOpt.get();
                String serie = venta.getTipoDocumento() == TipoDocumentoVenta.BOLETA
                        ? cfg.getSerieBoleta() : cfg.getSerieFactura();
                if (serie != null && !serie.isBlank()) {
                    numeroGenerado = secuenciaComprobanteService.siguienteNumeroConPrefijo(
                            sectorParaNumero, venta.getTipoDocumento(), serie);
                }
            }
        }

        venta.setNumeroDocumento(numeroGenerado != null ? numeroGenerado : dto.numeroDocumento());

        MetodoPago metodoPago = null;
        if (dto.metodoPagoId() != null) {
            MetodoPago mp = metodoPagoRepo.findById(dto.metodoPagoId())
                    .orElseThrow(() -> new BusinessException("Método de pago no existe"));
            venta.setMetodoPago(mp);
            metodoPago = mp;
        }

        var detalles = new ArrayList<VentaDetalle>();
        BigDecimal totalBruto = BigDecimal.ZERO;

        for (VentaItemDTO item : dto.items()) {
            Producto producto = productoRepo.findById(item.productoId())
                    .orElseThrow(() -> new BusinessException("Producto no existe: ID " + item.productoId()));
            verificarAccesoSector(producto.getSector());
            if (Boolean.TRUE.equals(producto.getEsInsumo())) {
                throw new BusinessException("No se puede vender un insumo. Use un producto vendible.");
            }

            if (producto.getStock() < item.cantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ", solicitado: " + item.cantidad() + ")");
            }

            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - item.cantidad());
            productoRepo.save(producto);

            inventarioService.registrarMovimiento(producto, TipoMovimiento.VENTA,
                    item.cantidad(), stockAnterior, producto.getStock(),
                    "Venta", null, usuario, usuario.getSede());

            VentaDetalle det = new VentaDetalle();
            det.setVenta(venta);
            det.setProducto(producto);
            det.setCantidad(item.cantidad());
            
            // Usar precio personalizado si viene en el DTO, sino usar el del catálogo
            BigDecimal precioUnitario = item.precioUnitario() != null 
                    ? item.precioUnitario() 
                    : producto.getPrecio();
            det.setPrecioUnitario(precioUnitario);

            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(item.cantidad()));
            totalBruto = totalBruto.add(subtotal);
            detalles.add(det);
        }

        venta.setDetalles(detalles);
        venta.setTotalBruto(totalBruto);

        // Promoción: por cada 10 compras calificadas (total bruto > 50) del mismo cliente en el mismo sector,
        // aplicar 10% de descuento en la compra #10 y repetir (20, 30...).
        BigDecimal minCompra = new BigDecimal("50.00");
        if (totalBruto != null && totalBruto.compareTo(minCompra) > 0) {
            Long clienteId = venta.getCliente() != null ? venta.getCliente().getId() : null;
            Long sectorId = venta.getSector() != null ? venta.getSector().getId() : null;

            long visitasAntes = (clienteId != null && sectorId != null)
                    ? ventaRepo.countComprasCalificadasParaPromocion(
                            clienteId,
                            sectorId,
                            minCompra,
                            EstadoVenta.ANULADA
                    )
                    : 0L;

            long visitaActual = visitasAntes + 1;
            boolean aplicaDescuento = visitaActual % 10 == 0;

            if (aplicaDescuento) {
                BigDecimal factor = new BigDecimal("0.80"); // 20% dto
                BigDecimal totalNeto = BigDecimal.ZERO;

                for (VentaDetalle det : detalles) {
                    BigDecimal precioUnitarioSinDescuento = det.getPrecioUnitario() != null
                            ? det.getPrecioUnitario()
                            : BigDecimal.ZERO;
                    BigDecimal precioUnitarioConDescuento = precioUnitarioSinDescuento
                            .multiply(factor)
                            .setScale(2, RoundingMode.HALF_UP);
                    det.setPrecioUnitario(precioUnitarioConDescuento);
                    totalNeto = totalNeto.add(precioUnitarioConDescuento.multiply(BigDecimal.valueOf(det.getCantidad())));
                }

                venta.setTotal(totalNeto);
                venta.setDescuentoPromocionVisitasPorcentaje(new BigDecimal("20.00"));
                venta.setDescuentoPromocionVisitasMonto(
                        totalBruto.subtract(totalNeto).setScale(2, RoundingMode.HALF_UP)
                );
                log.info("Promoción visitas aplicada: cliente={}, sector={}, visitaActual={}, descuento=20%",
                        clienteId, sectorId, visitaActual);
            } else {
                venta.setTotal(totalBruto);
                venta.setDescuentoPromocionVisitasMonto(null);
                venta.setDescuentoPromocionVisitasPorcentaje(null);
            }
        } else {
            venta.setTotal(totalBruto);
            venta.setDescuentoPromocionVisitasMonto(null);
            venta.setDescuentoPromocionVisitasPorcentaje(null);
        }

        // Si el método de pago es Yape (vía PayU) y se enviaron datos, procesar cobro antes de guardar la venta
        boolean esYape = metodoPago != null
                && metodoPago.getNombre() != null
                && metodoPago.getNombre().toLowerCase().contains("yape");
        if (esYape && dto.yapeTelefono() != null && !dto.yapeTelefono().isBlank()
                && dto.yapeOtp() != null && !dto.yapeOtp().isBlank()) {
            payuPaymentService.cobrarConYape(venta, dto.yapeTelefono().trim(), dto.yapeOtp().trim());
        }

        Venta guardada = ventaRepo.save(venta);

        LocalDateTime fechaVenta = guardada.getFecha();
        for (VentaDetalle det : guardada.getDetalles()) {
            HistorialPedido hp = new HistorialPedido();
            hp.setVenta(guardada);
            hp.setProducto(det.getProducto());
            hp.setNombreProducto(det.getProducto().getNombre());
            hp.setCantidad(det.getCantidad());
            hp.setPrecioUnitario(det.getPrecioUnitario());
            hp.setSubtotal(det.getPrecioUnitario().multiply(BigDecimal.valueOf(det.getCantidad())));
            hp.setFecha(fechaVenta);
            hp.setSector(guardada.getSector());
            historialPedidoRepo.save(hp);
        }

        log.info("Venta #{} creada por {} - Total: {} - Cliente: {}",
                guardada.getId(), usuario.getUsername(), guardada.getTotal(), cliente.getNombre());

        // Hook para acumulación de puntos CMR (stub: solo loggea si viene DNI y el método de pago es CMR)
        if (dto.dniCmr() != null && !dto.dniCmr().isBlank()
                && guardada.getMetodoPago() != null
                && "CMR".equalsIgnoreCase(guardada.getMetodoPago().getNombre())) {
            log.info("Solicitud de acumulación de puntos CMR para DNI {} en venta #{} ({} puntos configurables).",
                    dto.dniCmr(), guardada.getId(), "100");
        }

        // ── Envío a SUNAT (SEE del Contribuyente) ─────────────────────────
        // Se registra como afterCommit: se ejecuta DESPUÉS de que la transacción
        // de crear() hace commit, garantizando que la venta ya está visible en BD
        // cuando enviarComprobante (REQUIRES_NEW) la intenta leer y actualizar.
        final Long ventaId = guardada.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    sunatVentaService.enviarComprobante(ventaId);
                } catch (Exception e) {
                    log.error("Error enviando venta #{} a SUNAT tras commit: {}", ventaId, e.getMessage());
                }
            }
        });

        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> listar(Pageable pageable, Long sectorId) {
        Pageable conOrden = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "fecha"));
        Long effectiveSectorId = resolverSectorId(sectorId);
        if (effectiveSectorId != null) {
            return ventaRepo.findBySectorId(effectiveSectorId, conOrden).map(this::toResponseDTO);
        }
        return ventaRepo.findAll(conOrden).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());
        return toResponseDTO(venta);
    }

    @Override
    @Transactional
    public VentaResponseDTO anular(Long id) {
        Venta venta = ventaRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());

        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta ya está anulada");
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        for (VentaDetalle detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior + detalle.getCantidad());
            productoRepo.save(producto);

            inventarioService.registrarMovimiento(producto, TipoMovimiento.ANULACION_VENTA,
                    detalle.getCantidad(), stockAnterior, producto.getStock(),
                    "Anulación venta #" + id, id, usuario, venta.getSector());
        }

        venta.setEstado(EstadoVenta.ANULADA);
        Venta guardada = ventaRepo.save(venta);

        log.info("Venta #{} anulada por {}", id, usuario.getUsername());
        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> listarEntregasPendientes(Pageable pageable, Long sectorId, String tipoEntrega) {
        TipoEntrega tipo = null;
        if (tipoEntrega != null && !tipoEntrega.isBlank()) {
            try {
                tipo = TipoEntrega.valueOf(tipoEntrega.toUpperCase().trim());
            } catch (IllegalArgumentException ignored) {
                // Si es inválido, no filtra por tipo
            }
        }

        Long effectiveSectorId = resolverSectorId(sectorId);

        if (tipo != null) {
            if (effectiveSectorId != null) {
                return ventaRepo.findEntregasConHistorialBySector(
                        tipo, effectiveSectorId, EstadoVenta.PENDIENTE, EstadoEntrega.ENTREGADO, pageable)
                        .map(this::toResponseDTO);
            }
            return ventaRepo.findEntregasConHistorial(
                    tipo, EstadoVenta.PENDIENTE, EstadoEntrega.ENTREGADO, pageable)
                    .map(this::toResponseDTO);
        }

        if (effectiveSectorId != null) {
            return ventaRepo.findByRequiereDeliveryTrueAndEstadoAndSectorId(EstadoVenta.PENDIENTE, effectiveSectorId, pageable)
                    .map(this::toResponseDTO);
        }
        return ventaRepo.findByRequiereDeliveryTrueAndEstado(EstadoVenta.PENDIENTE, pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public VentaResponseDTO marcarEntregado(Long ventaId) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());

        if (!Boolean.TRUE.equals(venta.getRequiereDelivery())) {
            throw new BusinessException("Esta venta no tiene delivery");
        }
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("No se puede marcar entregada una venta anulada");
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        venta.setEstadoEntrega(EstadoEntrega.ENTREGADO);
        venta.setEstado(EstadoVenta.COMPLETADA);
        venta.setUsuarioEntrega(usuario);
        Venta guardada = ventaRepo.save(venta);

        log.info("Entrega de venta #{} marcada como entregada por {}", ventaId, usuario.getUsername());
        return toResponseDTO(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricasEntregasVendedorDTO> metricasEntregasPorVendedor(Long sectorId) {
        List<Venta> entregadas = ventaRepo.findByRequiereDeliveryTrueAndEstadoEntrega(EstadoEntrega.ENTREGADO);
        if (sectorId != null) {
            entregadas = entregadas.stream()
                    .filter(v -> v.getSector() != null && sectorId.equals(v.getSector().getId()))
                    .toList();
        }
        Map<Long, List<Venta>> porVendedor = entregadas.stream()
                .filter(v -> v.getUsuario() != null)
                .collect(Collectors.groupingBy(v -> v.getUsuario().getId()));
        return porVendedor.entrySet().stream()
                .map(e -> {
                    List<Venta> ventas = e.getValue();
                    BigDecimal total = ventas.stream()
                            .map(v -> Objects.requireNonNullElse(v.getTotal(), BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    String nombre = ventas.get(0).getUsuario().getUsername();
                    return new MetricasEntregasVendedorDTO(
                            nombre,
                            e.getKey(),
                            ventas.size(),
                            total
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticaEntregaDetalleDTO> metricasLogisticaEntregas(
            Long sectorId,
            Long vendedorId,
            String distrito,
            String provincia,
            String pais
    ) {
        List<Venta> entregadas = ventaRepo.findByRequiereDeliveryTrueAndEstadoEntrega(EstadoEntrega.ENTREGADO);

        if (sectorId != null) {
            entregadas = entregadas.stream()
                    .filter(v -> v.getSector() != null && sectorId.equals(v.getSector().getId()))
                    .toList();
        }

        String distritoLower = distrito != null ? distrito.trim().toLowerCase() : null;
        String provinciaLower = provincia != null ? provincia.trim().toLowerCase() : null;
        String paisLower = pais != null ? pais.trim().toLowerCase() : null;

        return entregadas.stream()
                .filter(v -> vendedorId == null || (v.getUsuario() != null && vendedorId.equals(v.getUsuario().getId())))
                .flatMap(v -> v.getDetalles().stream().map(det -> {
                    Cliente c = v.getCliente();
                    String cDistrito = c != null && c.getDistrito() != null ? c.getDistrito() : "";
                    String cProvincia = c != null && c.getProvincia() != null ? c.getProvincia() : "";
                    String cPais = c != null && c.getPais() != null ? c.getPais() : "";

                    if (distritoLower != null && !cDistrito.toLowerCase().contains(distritoLower)) {
                        return null;
                    }
                    if (provinciaLower != null && !cProvincia.toLowerCase().contains(provinciaLower)) {
                        return null;
                    }
                    if (paisLower != null && !cPais.toLowerCase().contains(paisLower)) {
                        return null;
                    }

                    BigDecimal subtotal = det.getPrecioUnitario()
                            .multiply(BigDecimal.valueOf(det.getCantidad()));

                    return new LogisticaEntregaDetalleDTO(
                            v.getId(),
                            v.getFecha(),
                            v.getUsuario() != null ? v.getUsuario().getId() : null,
                            v.getUsuario() != null ? v.getUsuario().getUsername() : null,
                            c != null ? c.getId() : null,
                            c != null ? c.getNombre() : null,
                            cDistrito,
                            cProvincia,
                            cPais,
                            det.getProducto() != null ? det.getProducto().getNombre() : null,
                            det.getCantidad(),
                            subtotal
                    );
                }))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public VentaResponseDTO solicitarTracking(Long ventaId) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());
        if (!Boolean.TRUE.equals(venta.getRequiereDelivery())) {
            throw new BusinessException("Esta venta no tiene delivery");
        }
        String codigo = "TRK-" + ventaId + "-" + System.currentTimeMillis();
        venta.setCodigoTracking(codigo);
        Venta guardada = ventaRepo.save(venta);
        log.info("Tracking solicitado para venta #{}: {}", ventaId, codigo);
        return toResponseDTO(guardada);
    }

    @Override
    @Transactional
    public VentaResponseDTO confirmarEntrega(Long ventaId, ConfirmacionEntregaRequestDTO dto) {
        Venta venta = ventaRepo.findById(ventaId)
                .orElseThrow(() -> new BusinessException("Venta no existe"));
        verificarAccesoSector(venta.getSector());
        if (!Boolean.TRUE.equals(venta.getRequiereDelivery())) {
            throw new BusinessException("Esta venta no tiene delivery");
        }
        venta.setConfirmacionFirma(dto.firmaBase64());
        venta.setConfirmacionCorreo(dto.correo());
        venta.setConfirmacionTelefono(dto.telefono());
        venta.setConfirmacionFecha(java.time.LocalDateTime.now());
        Venta guardada = ventaRepo.save(venta);
        log.info("Confirmación de entrega registrada para venta #{} - correo: {}", ventaId, dto.correo());
        return toResponseDTO(guardada);
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdUsuario != null && sectorEntidad != null
                && !sectorIdUsuario.equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
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
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    @Override
    public long contarComprasCalificadas(Long clienteId) {
        Long sectorId = obtenerSectorIdAutenticado();
        BigDecimal minCompra = new BigDecimal("50.00");
        return ventaRepo.countComprasCalificadasParaPromocion(
                clienteId,
                sectorId,
                minCompra,
                EstadoVenta.ANULADA
        );
    }

    private VentaResponseDTO toResponseDTO(Venta v) {
        List<VentaResponseDTO.VentaItemResponseDTO> items = v.getDetalles().stream()
                .map(d -> new VentaResponseDTO.VentaItemResponseDTO(
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
                ))
                .toList();

        return new VentaResponseDTO(
                v.getId(),
                v.getCliente().getId(),
                v.getCliente().getNombre(),
                v.getUsuario().getUsername(),
                v.getSector() != null ? v.getSector().getId() : null,
                v.getSector() != null ? v.getSector().getNombreSector() : null,
                v.getMetodoPago() != null ? v.getMetodoPago().getNombre() : null,
                v.getFecha(),
                v.getTotal(),
                v.getEstado().name(),
                v.getTipoDocumento() != null ? v.getTipoDocumento().name() : null,
                v.getNumeroDocumento(),
                v.getObservaciones(),
                v.getMoneda(),
                v.getConCuotas(),
                v.getRequiereDelivery(),
                v.getTipoEntrega() != null ? v.getTipoEntrega().name() : null,
                v.getDireccionEntrega(),
                v.getEstadoEntrega() != null ? v.getEstadoEntrega().name() : null,
                v.getUsuarioEntrega() != null ? v.getUsuarioEntrega().getUsername() : null,
                v.getCodigoTracking(),
                v.getConfirmacionCorreo(),
                v.getConfirmacionTelefono(),
                v.getConfirmacionFecha(),
                items,
                v.getSunatEstadoCdr() != null ? v.getSunatEstadoCdr().name() : null,
                v.getSunatCodigoRespuesta(),
                v.getSunatDescripcionCdr(),
                v.getSunatFechaEnvio(),
                v.getSunatNombreArchivo()
        );
    }
}
