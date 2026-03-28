package com.antecsis.service.sunat;

import com.antecsis.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Servicio que comunica con los WebServices SOAP de SUNAT:
 * - sendBill: para facturas, notas de crédito/débito (síncrono → devuelve CDR)
 * - sendSummary: para resumen diario de boletas y comunicaciones de baja (asíncrono → devuelve ticket)
 * - getStatus: consulta estado de un ticket (resumen diario)
 */
@Slf4j
@Service
public class SunatSoapService {

    @Value("${sunat.beta.endpoint-factura}")
    private String endpointBetaFactura;

    @Value("${sunat.produccion.endpoint-factura}")
    private String endpointProdFactura;

    @Value("${sunat.beta.endpoint-resumen}")
    private String endpointBetaResumen;

    @Value("${sunat.produccion.endpoint-resumen}")
    private String endpointProdResumen;

    private static final int TIMEOUT_SEGUNDOS = 60;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
            .build();

    /**
     * Envía un comprobante (factura, NCR, NDB) a SUNAT y retorna el CDR.
     * Síncrono: SUNAT responde en la misma llamada.
     */
    public SunatCdrResult sendBill(String solUsuario, String solClave,
                                   String xmlFirmado, String nombreArchivo,
                                   String ambiente) {
        try {
            byte[] zipBytes = empacarEnZip(xmlFirmado, nombreArchivo + ".xml");
            String zipBase64 = Base64.getEncoder().encodeToString(zipBytes);
            String endpoint = "produccion".equals(ambiente) ? endpointProdFactura : endpointBetaFactura;

            String soapBody = buildSendBillSoap(solUsuario, solClave, nombreArchivo + ".zip", zipBase64);
            String respuesta = invocarSoap(endpoint, soapBody, "sendBill");

            return parsearCdrRespuesta(respuesta);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error en sendBill SUNAT: {}", e.getMessage(), e);
            return SunatCdrResult.errorEnvio("Error de comunicación SUNAT: " + e.getMessage());
        }
    }

    /**
     * Envía el resumen diario de boletas o comunicación de baja.
     * Asíncrono: SUNAT devuelve un ticket; consultar con getStatus.
     */
    public SunatCdrResult sendSummary(String solUsuario, String solClave,
                                      String xmlFirmado, String nombreArchivo,
                                      String ambiente) {
        try {
            byte[] zipBytes = empacarEnZip(xmlFirmado, nombreArchivo + ".xml");
            String zipBase64 = Base64.getEncoder().encodeToString(zipBytes);
            String endpoint = "produccion".equals(ambiente) ? endpointProdResumen : endpointBetaResumen;

            String soapBody = buildSendSummarySoap(solUsuario, solClave, nombreArchivo + ".zip", zipBase64);
            String respuesta = invocarSoap(endpoint, soapBody, "sendSummary");

            return parsearTicketRespuesta(respuesta);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error en sendSummary SUNAT: {}", e.getMessage(), e);
            return SunatCdrResult.errorEnvio("Error de comunicación SUNAT: " + e.getMessage());
        }
    }

    /**
     * Consulta el estado de un resumen diario enviado previamente (por ticket).
     */
    public SunatCdrResult getStatus(String solUsuario, String solClave,
                                    String ticket, String ambiente) {
        try {
            String endpoint = "produccion".equals(ambiente) ? endpointProdResumen : endpointBetaResumen;
            String soapBody = buildGetStatusSoap(solUsuario, solClave, ticket);
            String respuesta = invocarSoap(endpoint, soapBody, "getStatus");
            return parsearGetStatusRespuesta(respuesta);

        } catch (Exception e) {
            log.error("Error en getStatus SUNAT: {}", e.getMessage(), e);
            return SunatCdrResult.errorEnvio("Error consultando ticket: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Construcción del XML SOAP
    // ─────────────────────────────────────────────────────────────────────

    private String buildSendBillSoap(String usuario, String clave, String fileName, String contentBase64) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:br="http://service.sunat.gob.pe"
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
              <soapenv:Header>
                <wsse:Security>
                  <wsse:UsernameToken>
                    <wsse:Username>%s</wsse:Username>
                    <wsse:Password>%s</wsse:Password>
                  </wsse:UsernameToken>
                </wsse:Security>
              </soapenv:Header>
              <soapenv:Body>
                <br:sendBill>
                  <fileName>%s</fileName>
                  <contentFile>%s</contentFile>
                </br:sendBill>
              </soapenv:Body>
            </soapenv:Envelope>""".formatted(usuario, clave, fileName, contentBase64);
    }

    private String buildSendSummarySoap(String usuario, String clave, String fileName, String contentBase64) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:br="http://service.sunat.gob.pe"
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
              <soapenv:Header>
                <wsse:Security>
                  <wsse:UsernameToken>
                    <wsse:Username>%s</wsse:Username>
                    <wsse:Password>%s</wsse:Password>
                  </wsse:UsernameToken>
                </wsse:Security>
              </soapenv:Header>
              <soapenv:Body>
                <br:sendSummary>
                  <fileName>%s</fileName>
                  <contentFile>%s</contentFile>
                </br:sendSummary>
              </soapenv:Body>
            </soapenv:Envelope>""".formatted(usuario, clave, fileName, contentBase64);
    }

    private String buildGetStatusSoap(String usuario, String clave, String ticket) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soapenv:Envelope
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:br="http://service.sunat.gob.pe"
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
              <soapenv:Header>
                <wsse:Security>
                  <wsse:UsernameToken>
                    <wsse:Username>%s</wsse:Username>
                    <wsse:Password>%s</wsse:Password>
                  </wsse:UsernameToken>
                </wsse:Security>
              </soapenv:Header>
              <soapenv:Body>
                <br:getStatus>
                  <ticket>%s</ticket>
                </br:getStatus>
              </soapenv:Body>
            </soapenv:Envelope>""".formatted(usuario, clave, ticket);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HTTP + parseo de respuestas
    // ─────────────────────────────────────────────────────────────────────

    private String invocarSoap(String endpoint, String soapBody, String action) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(TIMEOUT_SEGUNDOS))
                .header("Content-Type", "text/xml; charset=UTF-8")
                .header("SOAPAction", action)
                .POST(HttpRequest.BodyPublishers.ofString(soapBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.debug("SUNAT {} response status: {}", action, response.statusCode());

        if (response.statusCode() >= 500) {
            // SUNAT devuelve HTTP 500 con un SOAP Fault en el body que contiene el error real.
            // Si el body tiene un faultstring lo retornamos para que el parser lo extraiga.
            // Si no hay body útil, lanzamos con el status.
            String body = response.body();
            if (body != null && (body.contains("faultstring") || body.contains("Fault"))) {
                log.warn("SUNAT {} devolvió HTTP 500 con SOAP Fault — parseando body para extraer error real", action);
                return body;
            }
            throw new BusinessException("SUNAT devolvió error HTTP " + response.statusCode()
                    + (body != null && !body.isBlank() ? ": " + body.substring(0, Math.min(300, body.length())) : ""));
        }
        return response.body();
    }

    /** Parsea la respuesta de sendBill: extrae ZIP CDR en base64 → lee ResponseCode */
    private SunatCdrResult parsearCdrRespuesta(String soapRespuesta) {
        try {
            Document doc = parsearXml(soapRespuesta);

            // En caso de Fault
            NodeList faults = doc.getElementsByTagName("faultstring");
            if (faults.getLength() > 0) {
                return SunatCdrResult.errorEnvio("SUNAT Fault: " + faults.item(0).getTextContent());
            }

            // applicationResponse = ZIP en Base64
            NodeList appResp = doc.getElementsByTagName("applicationResponse");
            if (appResp.getLength() == 0) {
                log.warn("SUNAT no devolvió applicationResponse. Respuesta completa: {}",
                        soapRespuesta.substring(0, Math.min(500, soapRespuesta.length())));
                return SunatCdrResult.errorEnvio("SUNAT no devolvió applicationResponse");
            }

            // Quitar espacios y saltos de línea del Base64 (SUNAT puede formatear el contenido)
            String zipBase64 = appResp.item(0).getTextContent().replaceAll("\\s", "");
            log.debug("applicationResponse Base64 length: {} chars", zipBase64.length());

            if (zipBase64.isEmpty()) {
                log.warn("SUNAT devolvió applicationResponse vacío");
                return SunatCdrResult.errorEnvio("SUNAT devolvió CDR vacío");
            }

            String cdrXml = desempacarZip(zipBase64);

            if (cdrXml.isBlank()) {
                log.warn("CDR extraído del ZIP está vacío");
                return SunatCdrResult.errorEnvio("El CDR extraído del ZIP está vacío");
            }

            log.debug("CDR XML extraído (primeros 300 chars): {}", cdrXml.substring(0, Math.min(300, cdrXml.length())));
            return parsearCdrXml(cdrXml);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            return SunatCdrResult.errorEnvio("Error parseando respuesta SUNAT: " + e.getMessage());
        }
    }

    /** Parsea la respuesta de sendSummary: extrae el ticket */
    private SunatCdrResult parsearTicketRespuesta(String soapRespuesta) {
        try {
            Document doc = parsearXml(soapRespuesta);
            NodeList faults = doc.getElementsByTagName("faultstring");
            if (faults.getLength() > 0) {
                return SunatCdrResult.errorEnvio("SUNAT Fault: " + faults.item(0).getTextContent());
            }
            NodeList tickets = doc.getElementsByTagName("ticket");
            if (tickets.getLength() > 0) {
                return SunatCdrResult.ticket(tickets.item(0).getTextContent().trim());
            }
            return SunatCdrResult.errorEnvio("SUNAT no devolvió ticket");
        } catch (Exception e) {
            return SunatCdrResult.errorEnvio("Error parseando ticket SUNAT: " + e.getMessage());
        }
    }

    /**
     * Parsea la respuesta de getStatus del BillConsultService de SUNAT.
     * Estructura real de la respuesta:
     *   <getStatusResponse><return>
     *     <statusCode>0</statusCode>           ← 0=procesado, 98=en proceso, 99=en proceso
     *     <content>BASE64_ZIP_CDR</content>    ← presente cuando statusCode=0
     *   </return></getStatusResponse>
     */
    private SunatCdrResult parsearGetStatusRespuesta(String soapRespuesta) {
        try {
            Document doc = parsearXml(soapRespuesta);

            // SUNAT usa <statusCode> (no <status>)
            NodeList statusCodes = doc.getElementsByTagName("statusCode");
            if (statusCodes.getLength() > 0) {
                String code = statusCodes.item(0).getTextContent().trim();
                // 98 y 99 = aún en proceso, esperar próximo ciclo del scheduler
                if ("98".equals(code) || "99".equals(code)) {
                    return SunatCdrResult.builder()
                            .codigoRespuesta(code)
                            .descripcion("En proceso")
                            .build();
                }
            }

            // Cuando statusCode=0 SUNAT devuelve el CDR en <content> (no <applicationResponse>)
            NodeList content = doc.getElementsByTagName("content");
            if (content.getLength() > 0) {
                String zipBase64 = content.item(0).getTextContent().replaceAll("\\s", "");
                if (!zipBase64.isEmpty()) {
                    String cdrXml = desempacarZip(zipBase64);
                    if (!cdrXml.isBlank()) return parsearCdrXml(cdrXml);
                }
            }

            // Fallback: algunos SDKs devuelven <applicationResponse>
            NodeList appResp = doc.getElementsByTagName("applicationResponse");
            if (appResp.getLength() > 0) {
                String zipBase64 = appResp.item(0).getTextContent().replaceAll("\\s", "");
                if (!zipBase64.isEmpty()) {
                    String cdrXml = desempacarZip(zipBase64);
                    if (!cdrXml.isBlank()) return parsearCdrXml(cdrXml);
                }
            }

            return SunatCdrResult.errorEnvio("Respuesta getStatus inesperada: " + soapRespuesta.substring(0, Math.min(200, soapRespuesta.length())));
        } catch (Exception e) {
            return SunatCdrResult.errorEnvio("Error parseando getStatus: " + e.getMessage());
        }
    }

    /** Lee el XML del CDR y extrae código de respuesta y descripción */
    private SunatCdrResult parsearCdrXml(String cdrXml) {
        try {
            Document cdr = parsearXml(cdrXml);
            NodeList codes = cdr.getElementsByTagName("cbc:ResponseCode");
            if (codes.getLength() == 0) {
                codes = cdr.getElementsByTagName("ResponseCode");
            }
            String code = codes.getLength() > 0 ? codes.item(0).getTextContent().trim() : "?";

            NodeList descs = cdr.getElementsByTagName("cbc:Description");
            if (descs.getLength() == 0) descs = cdr.getElementsByTagName("Description");
            String desc = descs.getLength() > 0 ? descs.item(0).getTextContent().trim() : "";

            if ("0".equals(code)) {
                // Verificar si hay observaciones
                NodeList obs = cdr.getElementsByTagName("cac:DocumentResponse");
                return SunatCdrResult.builder()
                        .codigoRespuesta(code).descripcion(desc)
                        .aceptado(true)
                        .conObservaciones(obs.getLength() > 0)
                        .build();
            }
            return SunatCdrResult.rechazado(code, desc);

        } catch (Exception e) {
            return SunatCdrResult.errorEnvio("Error parseando CDR XML: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ZIP helpers
    // ─────────────────────────────────────────────────────────────────────

    private byte[] empacarEnZip(String contenido, String nombreArchivo) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(nombreArchivo);
            zos.putNextEntry(entry);
            zos.write(contenido.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private String desempacarZip(String zipBase64) throws IOException {
        // getMimeDecoder() tolera saltos de línea y espacios en el Base64 (SUNAT formatea el XML)
        byte[] zipBytes = Base64.getMimeDecoder().decode(zipBase64);
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            // Iterar hasta encontrar la primera entrada que sea un archivo real (no directorio)
            while ((entry = zis.getNextEntry()) != null) {
                String nombre = entry.getName();
                log.debug("Entrada ZIP: {} (directorio={}, {} bytes comprimidos)",
                        nombre, entry.isDirectory(), entry.getCompressedSize());
                if (entry.isDirectory() || nombre.endsWith("/")) {
                    zis.closeEntry();
                    continue;
                }
                String contenido = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                if (!contenido.isBlank()) {
                    return contenido;
                }
                // Si la entrada no tiene contenido, seguir buscando
                log.debug("Entrada '{}' estaba vacía, buscando siguiente...", nombre);
                zis.closeEntry();
            }
            log.warn("ZIP de SUNAT no contiene ningún archivo con contenido XML");
            return "";
        }
    }

    private Document parsearXml(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
