package com.antecsis.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Ventas/Compras Bodegas - ANTECSIS")
                        .description("API REST para el sistema de compra y venta de productos (bodegas). " +
                                "Incluye: Dashboard, Ventas, Compras, Productos, Clientes, Proveedores, " +
                                "Usuarios, Sectores, Localizaciones, Historial de pedidos, Solicitudes de producto, Mensajes/CHAT. " +
                                "Autenticación JWT: use POST /api/auth/login y pegue el token en **Authorize**.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ANTECSIS")
                                .url("")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor local")))
                .tags(List.of(
                        new Tag().name("Auth").description("Login y autenticación JWT"),
                        new Tag().name("Dashboard").description("Resumen ventas día/mes/año, producto más vendido, pedidos facturados/anulados"),
                        new Tag().name("Ventas").description("Registro de ventas, listado, anulación (Factura/Boleta)"),
                        new Tag().name("Compras").description("Registro de compras y anulación"),
                        new Tag().name("Productos").description("CRUD productos (insumos): nombre, descripción, unidad, imagen, stock mínimo alerta"),
                        new Tag().name("Clientes").description("CRUD clientes"),
                        new Tag().name("Proveedores").description("CRUD proveedores"),
                        new Tag().name("Usuarios").description("CRUD usuarios, roles (Cajero, Ventas, Logística, Administración), licencias"),
                        new Tag().name("Categorías").description("Categorías de productos"),
                        new Tag().name("Sectores").description("Sectores/sedes (teléfono, dirección)"),
                        new Tag().name("Localizaciones").description("Localizaciones con descripción e imagen"),
                        new Tag().name("Historial pedidos").description("Historial de ventas por producto y por fechas"),
                        new Tag().name("Solicitudes producto").description("Solicitudes de producto (Logística), estado Emergente/Leve"),
                        new Tag().name("Mensajes").description("CHAT / mensajes entre usuarios (WebSocket)"),
                        new Tag().name("Inventario").description("Stock disponible y productos con stock bajo"),
                        new Tag().name("Reportes").description("Exportación Excel y PDF de ventas por fechas"),
                        new Tag().name("Métodos de pago").description("Listado de métodos de pago")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtenido de POST /api/auth/login")));
    }
}
