package com.antecsis.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.antecsis.config.PayuProperties;
import com.antecsis.entity.Venta;
import com.antecsis.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayuPaymentService {

    private final PayuProperties properties;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Lanza una petición de cobro con Yape a PayU (sandbox).
     *
     * @param venta         Venta ya construida (con total, cliente, moneda).
     * @param celularYape   Número de celular del cliente en Yape.
     * @param otpYape       Código OTP de Yape.
     */
    public void cobrarConYape(Venta venta, String celularYape, String otpYape) {
        if (venta == null || venta.getTotal() == null) {
            throw new BusinessException("No se puede procesar pago Yape: venta o total nulo.");
        }
        if (celularYape == null || celularYape.isBlank() || otpYape == null || otpYape.isBlank()) {
            throw new BusinessException("Celular y OTP de Yape son obligatorios para este método de pago.");
        }

        if (properties.getSandboxUrl() == null || properties.getApiLogin() == null
                || properties.getApiKey() == null || properties.getAccountId() == null
                || properties.getMerchantId() == null) {
            log.warn("Propiedades PayU incompletas. Omite llamada real a PayU.");
            throw new BusinessException("Configuración de PayU incompleta. Revise PAYU_* en variables de entorno.");
        }

        String referenceCode = "VENTA-" + System.currentTimeMillis();
        BigDecimal total = venta.getTotal().setScale(2, RoundingMode.HALF_UP);
        String currency = venta.getMoneda() != null ? venta.getMoneda() : "PEN";

        String signature = buildSignature(properties.getApiKey(), properties.getMerchantId(), referenceCode, total,
                currency);

        Map<String, Object> body = buildYapeRequestBody(venta, celularYape, otpYape, referenceCode, total, currency,
                signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(properties.getSandboxUrl(), entity, Map.class);
            validarRespuesta(response);
        } catch (RestClientException ex) {
            log.error("Error llamando a PayU Yape", ex);
            throw new BusinessException("No se pudo procesar el pago con Yape. Intente nuevamente.");
        }
    }

    @SuppressWarnings("unchecked")
    private void validarRespuesta(ResponseEntity<Map> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException("Error al procesar pago Yape con PayU.");
        }
        Map<String, Object> body = response.getBody();
        Object code = body.get("code");
        if (!"SUCCESS".equals(code)) {
            String message = String.valueOf(body.getOrDefault("error", "Pago Yape rechazado por PayU."));
            throw new BusinessException(message);
        }
        Object trObj = body.get("transactionResponse");
        if (!(trObj instanceof Map)) {
            throw new BusinessException("Respuesta inesperada de PayU.");
        }
        Map<String, Object> tr = (Map<String, Object>) trObj;
        String state = (String) tr.get("state");
        String responseMessage = (String) tr.get("responseMessage");
        if (!"APPROVED".equalsIgnoreCase(state)) {
            String msg = responseMessage != null && !responseMessage.isBlank()
                    ? responseMessage
                    : "Pago Yape rechazado: " + tr.getOrDefault("responseCode", "SIN_RESPUESTA");
            throw new BusinessException(msg);
        }
        log.info("Pago Yape aprobado. transactionId={}, responseCode={}, message={}",
                tr.get("transactionId"), tr.get("responseCode"), responseMessage);
    }

    private String buildSignature(String apiKey, String merchantId, String referenceCode, BigDecimal value,
            String currency) {
        String raw = String.join("~", apiKey, merchantId, referenceCode, value.toPlainString(), currency);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo MD5 no disponible", e);
        }
    }

    private Map<String, Object> buildYapeRequestBody(Venta venta, String celularYape, String otpYape,
            String referenceCode, BigDecimal total, String currency, String signature) {
        Map<String, Object> root = new HashMap<>();
        root.put("language", "es");
        root.put("command", "SUBMIT_TRANSACTION");
        root.put("test", properties.isTest());

        Map<String, Object> merchant = new HashMap<>();
        merchant.put("apiKey", properties.getApiKey());
        merchant.put("apiLogin", properties.getApiLogin());
        root.put("merchant", merchant);

        Map<String, Object> transaction = new HashMap<>();

        Map<String, Object> order = new HashMap<>();
        order.put("accountId", properties.getAccountId());
        order.put("referenceCode", referenceCode);
        order.put("description", "Venta ANTECSIS " + (venta.getId() != null ? venta.getId() : ""));
        order.put("language", "es");
        order.put("signature", signature);

        Map<String, Object> additionalValues = new HashMap<>();
        Map<String, Object> txValue = new HashMap<>();
        txValue.put("value", total);
        txValue.put("currency", currency);
        additionalValues.put("TX_VALUE", txValue);
        order.put("additionalValues", additionalValues);

        Map<String, Object> buyer = new HashMap<>();
        buyer.put("fullName", venta.getCliente().getNombre());
        buyer.put("emailAddress", venta.getCliente().getEmail() != null ? venta.getCliente().getEmail()
                : "cliente@example.com");
        buyer.put("contactPhone", celularYape);
        buyer.put("dniNumber", venta.getCliente().getDocumento() != null ? venta.getCliente().getDocumento() : "");

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("street1", "Lima");
        shippingAddress.put("city", "Lima");
        shippingAddress.put("state", "Lima y Callao");
        shippingAddress.put("country", "PE");
        shippingAddress.put("postalCode", "000000");
        shippingAddress.put("phone", celularYape);
        buyer.put("shippingAddress", shippingAddress);

        order.put("buyer", buyer);
        transaction.put("order", order);

        Map<String, Object> extraParameters = new HashMap<>();
        extraParameters.put("OTP", otpYape);
        transaction.put("extraParameters", extraParameters);

        Map<String, Object> payer = new HashMap<>();
        payer.put("fullName", venta.getCliente().getNombre());
        payer.put("emailAddress", buyer.get("emailAddress"));
        payer.put("contactPhone", celularYape);
        payer.put("dniNumber", buyer.get("dniNumber"));

        Map<String, Object> billingAddress = new HashMap<>();
        billingAddress.put("street1", "Lima");
        billingAddress.put("city", "Lima");
        billingAddress.put("state", "Lima y Callao");
        billingAddress.put("country", "PE");
        billingAddress.put("postalCode", "000000");
        billingAddress.put("phone", celularYape);
        payer.put("billingAddress", billingAddress);

        transaction.put("payer", payer);

        transaction.put("type", "AUTHORIZATION_AND_CAPTURE");
        transaction.put("paymentMethod", "YAPE");
        transaction.put("paymentCountry", "PE");
        transaction.put("ipAddress", "127.0.0.1");

        root.put("transaction", transaction);
        return root;
    }
}

