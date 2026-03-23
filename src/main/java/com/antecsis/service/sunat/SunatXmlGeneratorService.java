package com.antecsis.service.sunat;

import com.antecsis.entity.ConfiguracionFiscal;
import com.antecsis.entity.Venta;
import com.antecsis.entity.VentaDetalle;
import com.antecsis.exception.BusinessException;
import com.antecsis.service.NumeroALetrasService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el XML UBL 2.1 (sin firma) para comprobantes electrónicos SUNAT
 * usando plantillas Freemarker ubicadas en classpath:/templates/sunat/.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SunatXmlGeneratorService {

    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");
    private static final BigDecimal IGV_DIVISOR = new BigDecimal("1.18");
    private static final String UNIDAD_MEDIDA = "NIU";
    private static final String AFECTACION_IGV = "10"; // Gravado Oneroso

    private final NumeroALetrasService numeroALetras;

    private Configuration freemarker;

    @PostConstruct
    public void init() throws Exception {
        freemarker = new Configuration(Configuration.VERSION_2_3_33);
        freemarker.setClassForTemplateLoading(this.getClass(), "/templates/sunat");
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarker.setLogTemplateExceptions(false);
        freemarker.setWrapUncheckedExceptions(true);
    }

    /** Genera el XML de factura o boleta (sin firma digital). */
    public String generarXml(Venta venta, ConfiguracionFiscal cfg, int correlativo) {
        SunatComprobanteModel model = construirModelo(venta, cfg, correlativo);
        String templateName = "01".equals(model.getTipoDocumento()) ? "factura.ftl" : "boleta.ftl";
        return renderizar(templateName, model);
    }

    /** Genera el XML del Resumen Diario de Boletas (SummaryDocuments) para envío vía sendSummary. */
    public String generarResumenDiario(java.util.List<Venta> boletas, ConfiguracionFiscal cfg, java.time.LocalDate fecha) {
        try {
            Template template = freemarker.getTemplate("resumen-diario.ftl");
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("ruc", cfg.getRuc());
            data.put("razonSocial", cfg.getRazonSocial());
            data.put("fecha", fecha.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            data.put("boletas", boletas);
            data.put("serie", cfg.getSerieBoleta());
            java.io.StringWriter writer = new java.io.StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new com.antecsis.exception.BusinessException("Error generando resumen diario: " + e.getMessage());
        }
    }

    /**
     * Retorna el nombre del archivo según convención SUNAT obligatoria:
     * {RUC}-{TIPO_DOC}-{SERIE}-{CORRELATIVO_8_DIGITOS}
     * Ejemplo: 20123456789-01-F001-00000001
     */
    public String calcularNombreArchivo(Venta venta, ConfiguracionFiscal cfg, int correlativo) {
        String tipo = "FACTURA".equals(venta.getTipoDocumento().name()) ? "01" : "03";
        String serie = "01".equals(tipo) ? cfg.getSerieFactura() : cfg.getSerieBoleta();
        return String.format("%s-%s-%s-%08d", cfg.getRuc(), tipo, serie, correlativo);
    }

    /**
     * Retorna el nombre del archivo del Resumen Diario de Boletas según convención SUNAT:
     * {RUC}-RC-{YYYYMMDD}-{NRO_RESUMEN_3_DIGITOS}
     * Ejemplo: 20123456789-RC-20260317-001
     */
    public String calcularNombreArchivoResumen(ConfiguracionFiscal cfg, java.time.LocalDate fecha, int nroResumen) {
        String fechaStr = fecha.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("%s-RC-%s-%03d", cfg.getRuc(), fechaStr, nroResumen);
    }

    // ─────────────────────────────────────────────────────────────────────

    private SunatComprobanteModel construirModelo(Venta venta, ConfiguracionFiscal cfg, int correlativo) {
        boolean esFactura = "FACTURA".equals(venta.getTipoDocumento().name());
        String tipoDoc = esFactura ? "01" : "03";
        String serie = esFactura ? cfg.getSerieFactura() : cfg.getSerieBoleta();

        // Calcular líneas con IGV
        List<SunatComprobanteModel.Linea> lineas = new ArrayList<>();
        BigDecimal totalValorVenta = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;

        int num = 1;
        for (VentaDetalle det : venta.getDetalles()) {
            BigDecimal precioConIgv = det.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal cantidad = BigDecimal.valueOf(det.getCantidad());
            BigDecimal valorUnitarioSinIgv = precioConIgv.divide(IGV_DIVISOR, 2, RoundingMode.HALF_UP);
            BigDecimal valorVentaLinea = valorUnitarioSinIgv.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igvLinea = valorVentaLinea.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);

            totalValorVenta = totalValorVenta.add(valorVentaLinea);
            totalIgv = totalIgv.add(igvLinea);

            String codigo = det.getProducto() != null && det.getProducto().getCodigo() != null
                    ? det.getProducto().getCodigo() : "P" + det.getId();

            lineas.add(SunatComprobanteModel.Linea.builder()
                    .numero(num++)
                    .descripcion(det.getProducto() != null ? det.getProducto().getNombre() : "Producto")
                    .codigoProducto(codigo)
                    .unidadMedida(UNIDAD_MEDIDA)
                    .cantidad(cantidad)
                    .valorUnitarioSinIgv(valorUnitarioSinIgv)
                    .precioConIgv(precioConIgv)
                    .valorVentaLinea(valorVentaLinea)
                    .igvLinea(igvLinea)
                    .codigoAfectacionIgv(AFECTACION_IGV)
                    .build());
        }

        BigDecimal totalPagar = totalValorVenta.add(totalIgv).setScale(2, RoundingMode.HALF_UP);

        // Datos del receptor
        String receptorTipoDoc = mapearTipoDocCliente(venta.getCliente().getTipoDocumento());
        String receptorNumDoc = venta.getCliente().getDocumento() != null
                ? venta.getCliente().getDocumento() : "-";
        String receptorNombre = venta.getCliente().getNombre() != null
                ? venta.getCliente().getNombre() : "-";

        var fecha = venta.getFecha() != null ? venta.getFecha() : java.time.LocalDateTime.now();

        return SunatComprobanteModel.builder()
                .emisorRuc(cfg.getRuc())
                .emisorRazonSocial(cfg.getRazonSocial())
                .emisorNombreComercial(cfg.getNombreComercial() != null ? cfg.getNombreComercial() : cfg.getRazonSocial())
                .emisorDomicilio(cfg.getDomicilioFiscal() != null ? cfg.getDomicilioFiscal() : "--")
                .emisorUbigeo(cfg.getUbigeo() != null ? cfg.getUbigeo() : "150101")
                .emisorDistrito(cfg.getDistrito() != null ? cfg.getDistrito() : "LIMA")
                .emisorProvincia(cfg.getProvincia() != null ? cfg.getProvincia() : "LIMA")
                .emisorDepartamento(cfg.getDepartamento() != null ? cfg.getDepartamento() : "LIMA")
                .tipoDocumento(tipoDoc)
                .tipoDocumentoListId("0101")
                .serie(serie)
                .correlativo(correlativo)
                .fechaEmision(fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .horaEmision(fecha.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .moneda(venta.getMoneda() != null ? venta.getMoneda() : "PEN")
                .notaMontoLetras(numeroALetras.convertir(totalPagar, venta.getMoneda()))
                .receptorTipoDoc(receptorTipoDoc)
                .receptorNumDoc(receptorNumDoc)
                .receptorNombre(receptorNombre)
                .receptorDireccion(venta.getCliente().getDireccion() != null ? venta.getCliente().getDireccion() : null)
                .totalValorVenta(totalValorVenta)
                .totalIgv(totalIgv)
                .totalPagar(totalPagar)
                .formaPago("Contado")
                .lineas(lineas)
                .build();
    }

    private String mapearTipoDocCliente(String tipoDocCliente) {
        if (tipoDocCliente == null) return "0";
        return switch (tipoDocCliente.toUpperCase().trim()) {
            case "DNI", "1" -> "1";
            case "RUC", "6" -> "6";
            case "CE", "CARNET", "4" -> "4";
            case "PASAPORTE", "7" -> "7";
            default -> "0";
        };
    }

    private String renderizar(String templateName, SunatComprobanteModel model) {
        try {
            Template template = freemarker.getTemplate(templateName);
            Map<String, Object> data = new HashMap<>();
            data.put("m", model);
            StringWriter writer = new StringWriter();
            template.process(data, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error generando XML UBL: {}", e.getMessage(), e);
            throw new BusinessException("Error generando XML UBL 2.1: " + e.getMessage());
        }
    }
}
