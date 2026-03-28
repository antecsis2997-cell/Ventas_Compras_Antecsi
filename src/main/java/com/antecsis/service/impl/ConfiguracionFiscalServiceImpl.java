package com.antecsis.service.impl;

import com.antecsis.dto.sunat.ConfiguracionFiscalRequestDTO;
import com.antecsis.dto.sunat.ConfiguracionFiscalResponseDTO;
import com.antecsis.entity.ConfiguracionFiscal;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.ConfiguracionFiscalRepository;
import com.antecsis.repository.SectorRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.ConfiguracionFiscalService;
import com.antecsis.service.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfiguracionFiscalServiceImpl implements ConfiguracionFiscalService {

    private final ConfiguracionFiscalRepository repo;
    private final SectorRepository sectorRepo;
    private final UsuarioRepository usuarioRepo;
    private final CryptoService crypto;

    // ─────────────────────────────────────────────────────────────────────────
    // Listar
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<ConfiguracionFiscalResponseDTO> listarParaUsuario(String username) {
        Usuario usuario = resolverUsuario(username);

        if (esSuperusuario(usuario)) {
            // SUPERUSUARIO ve todas las configuraciones
            return repo.findAll().stream().map(this::toDTO).toList();
        }

        // ADMIN solo ve la configuración de su propio sector
        if (usuario.getSede() == null) {
            return List.of(); // Sin sede asignada, sin configuración
        }
        return repo.findBySectorId(usuario.getSede().getId())
                .map(this::toDTO)
                .map(List::of)
                .orElse(List.of());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Guardar (crear o actualizar)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ConfiguracionFiscalResponseDTO guardarParaUsuario(ConfiguracionFiscalRequestDTO dto, String username) {
        Usuario usuario = resolverUsuario(username);

        Long sectorIdDestino = dto.sectorId();

        if (!esSuperusuario(usuario)) {
            // ADMIN solo puede configurar su propio sector
            if (usuario.getSede() == null) {
                throw new BusinessException("No tiene una bodega asignada. Contacte al administrador del sistema.");
            }
            if (!usuario.getSede().getId().equals(sectorIdDestino)) {
                throw new BusinessException("Solo puede configurar la información fiscal de su propia bodega.");
            }
        }

        Sector sector = sectorRepo.findById(sectorIdDestino)
                .orElseThrow(() -> new BusinessException("Sector no encontrado"));

        ConfiguracionFiscal config = repo.findBySectorId(sectorIdDestino)
                .orElseGet(ConfiguracionFiscal::new);

        config.setSector(sector);
        config.setRuc(dto.ruc().trim());
        config.setRazonSocial(dto.razonSocial().trim());
        config.setNombreComercial(dto.nombreComercial() != null ? dto.nombreComercial().trim() : null);
        config.setDomicilioFiscal(dto.domicilioFiscal() != null ? dto.domicilioFiscal().trim() : null);
        config.setUbigeo(dto.ubigeo() != null ? dto.ubigeo().trim() : null);
        config.setDistrito(dto.distrito() != null ? dto.distrito().trim() : null);
        config.setProvincia(dto.provincia() != null ? dto.provincia().trim() : null);
        config.setDepartamento(dto.departamento() != null ? dto.departamento().trim() : null);
        config.setSolUsuarioCifrado(crypto.cifrar(dto.solUsuario()));
        config.setSolClaveCifrada(crypto.cifrar(dto.solClave()));

        // Las series solo las puede modificar el ADMIN de la bodega.
        // Si el SUPERUSUARIO guarda una config nueva, se inicializan con valores por defecto (B001/F001).
        // Si ya existe la config, el SUPERUSUARIO no puede cambiarlas.
        if (!esSuperusuario(usuario)) {
            config.setSerieBoleta(dto.serieBoleta().toUpperCase());
            config.setSerieFactura(dto.serieFactura().toUpperCase());
        } else if (config.getId() == null) {
            config.setSerieBoleta("B001");
            config.setSerieFactura("F001");
        }

        config.setAmbiente(dto.ambiente());
        config.setActivo(dto.activo());

        if (dto.certificadoPfxBase64() != null && !dto.certificadoPfxBase64().isBlank()) {
            config.setCertificadoPfxCifrado(crypto.cifrar(dto.certificadoPfxBase64()));
        }
        if (dto.certificadoClave() != null && !dto.certificadoClave().isBlank()) {
            config.setCertificadoClaveCifrada(crypto.cifrar(dto.certificadoClave()));
        }

        return toDTO(repo.save(config));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Activar / Desactivar
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ConfiguracionFiscalResponseDTO activarParaUsuario(Long id, String username) {
        ConfiguracionFiscal config = resolverConfigConPermisos(id, username);
        config.setActivo(true);
        return toDTO(repo.save(config));
    }

    @Override
    @Transactional
    public ConfiguracionFiscalResponseDTO desactivarParaUsuario(Long id, String username) {
        ConfiguracionFiscal config = resolverConfigConPermisos(id, username);
        config.setActivo(false);
        return toDTO(repo.save(config));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Uso interno SUNAT
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public Optional<ConfiguracionFiscal> buscarActivaPorSector(Long sectorId) {
        return repo.findBySectorIdAndActivoTrue(sectorId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private Usuario resolverUsuario(String username) {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    private boolean esSuperusuario(Usuario usuario) {
        return usuario.getRol() != null && "SUPERUSUARIO".equals(usuario.getRol().getNombre());
    }

    /**
     * Obtiene la configuración por ID y valida que el ADMIN solo acceda a la suya.
     */
    private ConfiguracionFiscal resolverConfigConPermisos(Long id, String username) {
        ConfiguracionFiscal config = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Configuración fiscal no encontrada"));

        Usuario usuario = resolverUsuario(username);
        if (!esSuperusuario(usuario)) {
            if (usuario.getSede() == null ||
                !usuario.getSede().getId().equals(config.getSector().getId())) {
                throw new BusinessException("No tiene permiso para modificar la configuración de otra bodega.");
            }
        }
        return config;
    }

    private ConfiguracionFiscalResponseDTO toDTO(ConfiguracionFiscal c) {
        return new ConfiguracionFiscalResponseDTO(
                c.getId(),
                c.getSector().getId(),
                c.getSector().getNombreSector(),
                c.getRuc(),
                c.getRazonSocial(),
                c.getNombreComercial(),
                c.getDomicilioFiscal(),
                c.getUbigeo(),
                c.getDistrito(),
                c.getProvincia(),
                c.getDepartamento(),
                c.getSolUsuarioCifrado() != null,
                c.getCertificadoPfxCifrado() != null,
                c.getSerieBoleta(),
                c.getSerieFactura(),
                c.getAmbiente(),
                c.isActivo()
        );
    }
}
