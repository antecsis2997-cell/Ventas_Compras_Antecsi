package com.antecsis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "suscripciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del cliente/empresa (ej. MARKET). */
    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    /** RUC del cliente. */
    @Column(length = 20)
    private String ruc;

    @ManyToOne
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    /** Rubro de negocio (Mercado, Zapatería, etc.); distinto del sector/sede operativo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rubro_comercial_id")
    private RubroComercial rubroComercial;

    /** Correo del administrador del plan (compra pública u onboarding). */
    @Column(name = "correo_admin", length = 200)
    private String correoAdmin;

    @Column(length = 500)
    private String descripcion;

    /** POR_RENOVAR, PAGADO, TRANSACCION_EN_PROCESO */
    @Column(nullable = false, length = 50)
    private String estado;

    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;

    /** PAQUETE_BASICO, etc. */
    @Column(length = 80)
    private String paquete;

    /** Correo al que se envía la alerta de vencimiento */
    @Column(name = "correo_receptor", length = 150)
    private String correoReceptor;

    /** Última vez que se envió alerta de vencimiento */
    @Column(name = "fecha_ultima_alerta")
    private LocalDateTime fechaUltimaAlerta;

    /** Última alerta de "próximo a vencer" (licencia/suscripción en los próximos días). */
    @Column(name = "fecha_ultima_alerta_proximo_vencimiento")
    private LocalDateTime fechaUltimaAlertaProximoVencimiento;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isVencida() {
        return fechaCaducidad != null && fechaCaducidad.isBefore(LocalDate.now());
    }
}
