package com.antecsis.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.suscripcion.SuscripcionRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionResponseDTO;
import com.antecsis.entity.ActivacionLicencia;
import com.antecsis.entity.RubroComercial;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Suscripcion;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ActivacionLicenciaRepository;
import com.antecsis.repository.RubroComercialRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.SuscripcionRepository;
import com.antecsis.security.LicenseJwtUtil;
import com.antecsis.service.EmailService;
import com.antecsis.service.NotificacionBandejaService;
import com.antecsis.service.SuscripcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private static final long DIAS_ENTRE_ALERTAS = 10;
    private static final long DIAS_ENTRE_ALERTAS_PROXIMAS = 5;

    private final SuscripcionRepository repo;
    private final SectorRepository sectorRepo;
    private final RubroComercialRepository rubroRepo;
    private final ActivacionLicenciaRepository activacionLicenciaRepository;
    private final EmailService emailService;
    private final LicenseJwtUtil licenseJwtUtil;
    private final NotificacionBandejaService notificacionBandejaService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.suscripciones.alerta-proximo-dias:7}")
    private int alertaProximoDias;

    @Override
    @Transactional
    public SuscripcionResponseDTO crear(SuscripcionRequestDTO dto) {
        Sector sector = sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new BusinessException("Sector no encontrado"));
        Suscripcion s = new Suscripcion();
        mapToEntity(s, dto, sector);
        s = repo.save(s);
        return toResponse(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SuscripcionResponseDTO> listar(Pageable pageable, String estado, Long rubroId) {
        Page<Suscripcion> page;
        if ((estado == null || estado.isBlank()) && rubroId == null) {
            page = repo.findByOrderByFechaCaducidadAsc(pageable);
        } else {
            String estadoParam = (estado != null && !estado.isBlank()) ? estado : null;
            page = repo.findFiltrada(estadoParam, rubroId, pageable);
        }
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SuscripcionResponseDTO obtenerPorId(Long id) {
        Suscripcion s = repo.findById(id).orElseThrow(() -> new BusinessException("Suscripción no encontrada"));
        return toResponse(s);
    }

    @Override
    @Transactional
    public SuscripcionResponseDTO actualizar(Long id, SuscripcionRequestDTO dto) {
        Suscripcion s = repo.findById(id).orElseThrow(() -> new BusinessException("Suscripción no encontrada"));
        Sector sector = sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new BusinessException("Sector no encontrado"));
        mapToEntity(s, dto, sector);
        s = repo.save(s);
        return toResponse(s);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new BusinessException("Suscripción no encontrada");
        }
        activacionLicenciaRepository.findBySuscripcion_Id(id).ifPresent(activacionLicenciaRepository::delete);
        repo.deleteById(id);
    }

    @Override
    @Transactional
    public void enviarAlerta(Long id) {
        Suscripcion s = repo.findById(id).orElseThrow(() -> new BusinessException("Suscripción no encontrada"));
        String correo = s.getCorreoReceptor();
        if (correo == null || correo.isBlank()) {
            throw new BusinessException("La suscripción no tiene correo receptor configurado");
        }
        String sucursal = s.getSector() != null ? s.getSector().getNombreSector() : "—";
        emailService.enviarAlertaSuscripcionVencida(correo, s.getNombreCliente(), sucursal, s.getFechaCaducidad());
        s.setFechaUltimaAlerta(LocalDateTime.now());
        repo.save(s);
    }

    @Override
    @Transactional
    public void ejecutarAlertasAutomaticas() {
        LocalDate hoy = LocalDate.now();
        List<Suscripcion> vencidas = repo.findByFechaCaducidadLessThanEqual(hoy);
        int enviadas = 0;
        for (Suscripcion s : vencidas) {
            String correo = s.getCorreoReceptor();
            if (correo == null || correo.isBlank()) continue;
            LocalDateTime ultima = s.getFechaUltimaAlerta();
            if (ultima != null && ChronoUnit.DAYS.between(ultima.toLocalDate(), hoy) < DIAS_ENTRE_ALERTAS) {
                continue;
            }
            try {
                String sucursal = s.getSector() != null ? s.getSector().getNombreSector() : "—";
                emailService.enviarAlertaSuscripcionVencida(correo, s.getNombreCliente(), sucursal, s.getFechaCaducidad());
                s.setFechaUltimaAlerta(LocalDateTime.now());
                repo.save(s);
                enviadas++;
            } catch (Exception e) {
                log.warn("No se pudo enviar alerta para suscripción {}: {}", s.getId(), e.getMessage());
            }
        }
        if (enviadas > 0) {
            log.info("Alertas de suscripción vencida: {} enviadas", enviadas);
        }
    }

    @Override
    @Transactional
    public void ejecutarAlertasProximoVencimiento() {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(alertaProximoDias);
        List<Suscripcion> proximas = repo.findByEstadoAndFechaCaducidadBetween("PAGADO", hoy.plusDays(1), fin);
        int enviadas = 0;
        for (Suscripcion s : proximas) {
            String correo = firstNonBlank(s.getCorreoAdmin(), s.getCorreoReceptor());
            if (correo == null || correo.isBlank()) {
                continue;
            }
            LocalDateTime ultima = s.getFechaUltimaAlertaProximoVencimiento();
            if (ultima != null && ChronoUnit.DAYS.between(ultima.toLocalDate(), hoy) < DIAS_ENTRE_ALERTAS_PROXIMAS) {
                continue;
            }
            int diasRest = (int) ChronoUnit.DAYS.between(hoy, s.getFechaCaducidad());
            if (diasRest < 1) {
                continue;
            }
            try {
                String sucursal = s.getSector() != null ? s.getSector().getNombreSector() : "—";
                emailService.enviarAlertaSuscripcionPorVencer(
                        correo.trim(), s.getNombreCliente(), sucursal, s.getFechaCaducidad(), diasRest);
                s.setFechaUltimaAlertaProximoVencimiento(LocalDateTime.now());
                repo.save(s);
                enviadas++;
            } catch (Exception e) {
                log.warn("No se pudo enviar alerta próximo vencimiento suscripción {}: {}", s.getId(), e.getMessage());
            }
        }
        if (enviadas > 0) {
            log.info("Alertas próximo vencimiento de licencia/suscripción: {} enviadas", enviadas);
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    @Override
    @Transactional
    public void compraPublica(String plan, String ruc, String nombreCliente, String correoAdministrador, String rubroCodigo,
                              String nombreTitularTarjeta, String numeroTarjeta, String fechaCaducidadTarjeta, Long sectorId) {
        Sector sector;
        if (sectorId != null && sectorId > 0) {
            sector = sectorRepo.findById(sectorId)
                    .orElse(sectorRepo.findAll().stream().findFirst().orElseThrow(() -> new BusinessException("No hay sectores configurados")));
        } else {
            sector = sectorRepo.findAll().stream().findFirst()
                    .orElseThrow(() -> new BusinessException("No hay sectores configurados. Contacte al administrador."));
        }
        String paquete = "PAQUETE_" + (plan != null ? plan.toUpperCase() : "BASICO");
        LocalDate caducidad = LocalDate.now().plusYears(1);
        Suscripcion s = new Suscripcion();
        s.setNombreCliente(nombreCliente);
        s.setRuc(ruc);
        s.setSector(sector);
        s.setDescripcion("Compra pública - Plan " + plan + ". Pago simulado (API bancaria pendiente). Titular: "
                + (nombreTitularTarjeta != null ? nombreTitularTarjeta : "—"));
        s.setEstado("PAGADO");
        s.setFechaCaducidad(caducidad);
        s.setPaquete(paquete);
        s.setCorreoReceptor(correoAdministrador != null ? correoAdministrador.trim() : null);
        s.setCorreoAdmin(correoAdministrador != null ? correoAdministrador.trim() : null);
        if (rubroCodigo != null && !rubroCodigo.isBlank()) {
            rubroRepo.findByCodigoIgnoreCase(rubroCodigo.trim())
                    .filter(RubroComercial::isActivo)
                    .ifPresent(s::setRubroComercial);
        }
        s = repo.save(s);

        String jti = UUID.randomUUID().toString();
        String jwt = licenseJwtUtil.generarLicencia(s.getId(), plan != null ? plan : "BASICO", jti, caducidad);

        ActivacionLicencia al = ActivacionLicencia.builder()
                .suscripcion(s)
                .jti(jti)
                .vigenciaHasta(caducidad)
                .activada(false)
                .build();
        activacionLicenciaRepository.save(al);

        String planEtiqueta = etiquetaPlan(paquete);
        String urlActivacion = frontendUrl.replaceAll("/$", "") + "/cuenta/licencia";

        try {
            emailService.enviarLicenciaPlan(correoAdministrador.trim(), nombreCliente, planEtiqueta, jwt, urlActivacion);
        } catch (Exception e) {
            log.warn("No se pudo enviar email de licencia (suscripción {} creada igualmente): {}", s.getId(), e.getMessage());
        }

        String preview = jwt != null && jwt.length() > 120 ? jwt.substring(0, 120) + "…" : jwt;
        notificacionBandejaService.registrar(
                correoAdministrador.trim(),
                "LICENCIA_PLAN",
                "Licencia emitida — Plan " + planEtiqueta,
                "Se generó su clave de licencia. Revise el correo o pegue el token en Cuenta → Licencia. " + preview
        );

        log.info("Compra pública registrada: plan={}, cliente={}, RUC={}, suscripcionId={}", plan, nombreCliente, ruc, s.getId());
    }

    private void mapToEntity(Suscripcion s, SuscripcionRequestDTO dto, Sector sector) {
        s.setNombreCliente(dto.nombreCliente());
        s.setRuc(dto.ruc());
        s.setSector(sector);
        s.setDescripcion(dto.descripcion());
        s.setEstado(dto.estado());
        s.setFechaCaducidad(dto.fechaCaducidad());
        s.setPaquete(dto.paquete());
        s.setCorreoReceptor(dto.correoReceptor());
        if (dto.correoReceptor() != null && !dto.correoReceptor().isBlank()) {
            s.setCorreoAdmin(dto.correoReceptor().trim());
        }
    }

    private SuscripcionResponseDTO toResponse(Suscripcion s) {
        String sucursal = s.getSector() != null ? s.getSector().getNombreSector() : null;
        String textoAlerta = null;
        if (s.getFechaUltimaAlerta() != null) {
            long dias = ChronoUnit.DAYS.between(s.getFechaUltimaAlerta().toLocalDate(), LocalDate.now());
            textoAlerta = "ALERTA ENVIADA HACE " + dias + " DÍAS";
        }
        Optional<ActivacionLicencia> act = activacionLicenciaRepository.findBySuscripcion_Id(s.getId());
        Boolean licenciaActivada = act.map(ActivacionLicencia::isActivada).orElse(false);
        String rubroNombre = s.getRubroComercial() != null ? s.getRubroComercial().getNombre() : null;
        return new SuscripcionResponseDTO(
                s.getId(),
                s.getNombreCliente(),
                s.getRuc(),
                s.getSector() != null ? s.getSector().getId() : null,
                sucursal,
                s.getDescripcion(),
                s.getEstado(),
                s.getFechaCaducidad(),
                s.getPaquete(),
                s.getCorreoReceptor(),
                s.getCorreoAdmin(),
                rubroNombre,
                licenciaActivada,
                s.getFechaUltimaAlerta(),
                textoAlerta
        );
    }

    private static String etiquetaPlan(String paquete) {
        if (paquete == null) return "—";
        String p = paquete.toUpperCase();
        if (p.contains("BASICO")) return "Básico";
        if (p.contains("INTERMEDIO")) return "Intermedio";
        if (p.contains("AVANZADO")) return "Premium";
        return paquete.replace("PAQUETE_", "").replace("_", " ");
    }
}
