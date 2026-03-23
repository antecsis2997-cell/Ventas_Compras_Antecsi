package com.antecsis.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Convierte un monto decimal a su representación en letras en español.
 * Ej: 118.00 → "CIENTO DIECIOCHO CON 00/100 SOLES"
 */
@Service
public class NumeroALetrasService {

    private static final String[] UNIDADES = {
        "", "UNO", "DOS", "TRES", "CUATRO", "CINCO",
        "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
        "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
        "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE",
        "VEINTIUN", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO",
        "VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"
    };

    private static final String[] DECENAS = {
        "", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA",
        "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };

    private static final String[] CENTENAS = {
        "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
        "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    public String convertir(BigDecimal monto, String moneda) {
        BigDecimal rounded = monto.setScale(2, RoundingMode.HALF_UP);
        long entero = rounded.longValue();
        int centavos = rounded.subtract(BigDecimal.valueOf(entero)).multiply(BigDecimal.valueOf(100)).intValue();

        String letras = numeroALetras(entero);
        String monedaStr = "PEN".equals(moneda) ? "SOLES" : "DOLARES AMERICANOS";
        return String.format("SON: %s CON %02d/100 %s", letras, centavos, monedaStr);
    }

    private String numeroALetras(long numero) {
        if (numero == 0) return "CERO";
        if (numero < 0) return "MENOS " + numeroALetras(-numero);

        String resultado = "";

        if (numero >= 1_000_000) {
            long millones = numero / 1_000_000;
            resultado += (millones == 1 ? "UN MILLON" : numeroALetras(millones) + " MILLONES");
            numero %= 1_000_000;
            if (numero > 0) resultado += " ";
        }

        if (numero >= 1_000) {
            long miles = numero / 1_000;
            resultado += (miles == 1 ? "MIL" : numeroALetras(miles) + " MIL");
            numero %= 1_000;
            if (numero > 0) resultado += " ";
        }

        if (numero >= 100) {
            int centena = (int) (numero / 100);
            if (numero == 100) {
                resultado += "CIEN";
            } else {
                resultado += CENTENAS[centena];
            }
            numero %= 100;
            if (numero > 0) resultado += " ";
        }

        if (numero >= 30) {
            int decena = (int) (numero / 10);
            resultado += DECENAS[decena];
            numero %= 10;
            if (numero > 0) resultado += " Y ";
        } else if (numero > 0) {
            resultado += UNIDADES[(int) numero];
            numero = 0;
        }

        if (numero > 0) {
            resultado += UNIDADES[(int) numero];
        }

        return resultado.trim();
    }
}
