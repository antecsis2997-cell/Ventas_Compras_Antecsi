package com.antecsis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payu")
public class PayuProperties {

    /**
     * URL del endpoint de pagos de PayU.
     * Ejemplo sandbox: https://sandbox.api.payulatam.com/payments-api/4.0/service.cgi
     */
    private String sandboxUrl;

    /**
     * apiLogin entregado por PayU.
     */
    private String apiLogin;

    /**
     * apiKey entregada por PayU.
     */
    private String apiKey;

    /**
     * accountId asociado a la cuenta de Perú.
     */
    private String accountId;

    /**
     * merchantId usado para la firma.
     */
    private String merchantId;

    /**
     * Indica si el modo prueba (sandbox) está habilitado.
     */
    private boolean test = true;
}

