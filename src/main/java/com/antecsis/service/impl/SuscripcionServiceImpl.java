package com.antecsis.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.suscripcion.SuscripcionRequestDTO;
import com.antecsis.dto.suscripcion.SuscripcionResponseDTO;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Suscripcion;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.SuscripcionRepository;
import com.antecsis.service.EmailService;
import com.antecsis.service.SuscripcionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {

    private static final long DIAS_ENTRE_ALERTAS = 10;

    private final SuscripcionRepository repo;
    private final SectorRepository sectorRepo;
    private final EmailService emailService;

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
    public Page<SuscripcionResponseDTO> listar(Pageable pageable, String estado) {
        Page<Suscripcion> page = estado != null && !estado.isBlank()
                ? repo.findByEstado(estado, pageable)
                : repo.findByOrderByFechaCaducidadAsc(pageable);
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
    public void compraPublica(String plan, String ruc, String nombreCliente, String nombreTitularTarjeta,
                              String numeroTarjeta, String fechaCaducidadTarjeta, Long sectorId) {
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
        s.setDescripcion("Compra pública - Plan " + plan + ". Pago simulado (API bancaria pendiente).");
        s.setEstado("PAGADO");
        s.setFechaCaducidad(caducidad);
        s.setPaquete(paquete);
        s.setCorreoReceptor(null);
        repo.save(s);
        log.info("Compra pública registrada: plan={}, cliente={}, RUC={}", plan, nombreCliente, ruc);
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
    }

    private SuscripcionResponseDTO toResponse(Suscripcion s) {
        String sucursal = s.getSector() != null ? s.getSector().getNombreSector() : null;
        String textoAlerta = null;
        if (s.getFechaUltimaAlerta() != null) {
            long dias = ChronoUnit.DAYS.between(s.getFechaUltimaAlerta().toLocalDate(), LocalDate.now());
            textoAlerta = "ALERTA ENVIADA HACE " + dias + " DÍAS";
        }
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
                s.getFechaUltimaAlerta(),
                textoAlerta
        );
    }
}
