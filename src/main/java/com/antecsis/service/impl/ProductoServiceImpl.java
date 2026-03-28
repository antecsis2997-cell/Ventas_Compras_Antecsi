package com.antecsis.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.antecsis.dto.producto.ProductoRequestDTO;
import com.antecsis.dto.producto.ProductoResponseDTO;
import com.antecsis.entity.Categoria;
import com.antecsis.entity.Producto;
import com.antecsis.entity.Sector;
import com.antecsis.entity.Usuario;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.CategoriaRepository;
import com.antecsis.repository.ProductoRepository;
import com.antecsis.repository.UsuarioRepository;
import com.antecsis.service.ProductoService;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Categoria cat = null;
        if (dto.categoriaId() != null) {
            cat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new BusinessException("Categoría no existe"));
            verificarAccesoSector(cat.getSector());
        }

        Long sectorIdUsuario = usuario.getSede() != null ? usuario.getSede().getId() : null;
        Long sectorUnicidad = sectorIdUsuario != null
                ? sectorIdUsuario
                : (cat != null && cat.getSector() != null ? cat.getSector().getId() : null);

        if (sectorUnicidad != null && dto.nombre() != null) {
            if (repository.existsByNombreAndSectorIdAndEsInsumo(dto.nombre(), sectorUnicidad, false)) {
                throw new BusinessException("Ya existe un producto con ese nombre en tu bodega");
            }
        }
        if (sectorUnicidad != null && dto.codigo() != null && !dto.codigo().isBlank()) {
            if (repository.existsByCodigoAndSectorIdAndEsInsumo(dto.codigo(), sectorUnicidad, false)) {
                throw new BusinessException("Ya existe un producto con ese código en tu bodega");
            }
        }

        Producto producto = new Producto();
        producto.setCodigo(dto.codigo());
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setPrecioCompra(dto.precioCompra());
        producto.setStock(dto.stock());
        producto.setMoneda(Objects.requireNonNullElse(dto.moneda(), "PEN"));
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setImagenUrl(normalizarImagenUrl(dto.imagenUrl()));
        producto.setStockMinimoAlerta(dto.stockMinimoAlerta());
        producto.setTipo(dto.tipo());
        producto.setMarca(dto.marca());
        producto.setCantidad(dto.cantidad());
        Sector sectorAsignado = usuario.getSede();
        if (sectorAsignado == null && cat != null) {
            sectorAsignado = cat.getSector();
        }
        producto.setSector(sectorAsignado);
        producto.setActivo(true);
        // /api/productos es SOLO para productos vendibles
        producto.setEsInsumo(false);
        producto.setCategoria(cat);

        Producto guardado = repository.save(producto);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listar(Pageable pageable) {
        Long sectorId = obtenerSectorIdAutenticado();
        if (sectorId != null) {
            // /api/productos debe listar SOLO productos vendibles (no insumos)
            return repository.findBySectorIdAndEsInsumo(sectorId, false, pageable).map(this::toResponseDTO);
        }
        return repository.findByEsInsumo(false, pageable).map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no existe"));
        verificarAccesoSector(producto.getSector());
        return toResponseDTO(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no existe"));
        verificarAccesoSector(producto.getSector());

        Usuario usuario = obtenerUsuarioAutenticado();
        Long sectorIdUsuario = usuario.getSede() != null ? usuario.getSede().getId() : null;

        Categoria nuevaCat = null;
        if (dto.categoriaId() != null) {
            nuevaCat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new BusinessException("Categoría no existe"));
            verificarAccesoSector(nuevaCat.getSector());
        }

        Long sectorUnicidad = sectorIdUsuario;
        if (sectorUnicidad == null && nuevaCat != null && nuevaCat.getSector() != null) {
            sectorUnicidad = nuevaCat.getSector().getId();
        }
        if (sectorUnicidad == null && producto.getSector() != null) {
            sectorUnicidad = producto.getSector().getId();
        }

        if (sectorUnicidad != null && dto.nombre() != null) {
            boolean existeOtroConMismoNombre = repository.findBySectorId(sectorUnicidad, Pageable.unpaged())
                    .stream()
                    .anyMatch(p -> !p.getId().equals(id)
                            && Boolean.FALSE.equals(p.getEsInsumo())
                            && p.getNombre().equals(dto.nombre()));
            if (existeOtroConMismoNombre) {
                throw new BusinessException("Ya existe otro producto con ese nombre en tu bodega");
            }
        }

        if (sectorUnicidad != null && dto.codigo() != null && !dto.codigo().isBlank()) {
            boolean existeOtroConMismoCodigo = repository.findBySectorId(sectorUnicidad, Pageable.unpaged())
                    .stream()
                    .anyMatch(p -> !p.getId().equals(id)
                            && Boolean.FALSE.equals(p.getEsInsumo())
                            && dto.codigo().equals(p.getCodigo()));
            if (existeOtroConMismoCodigo) {
                throw new BusinessException("Ya existe otro producto con ese código en tu bodega");
            }
        }

        producto.setCodigo(dto.codigo());
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setPrecioCompra(dto.precioCompra());
        producto.setStock(dto.stock());
        producto.setMoneda(Objects.requireNonNullElse(dto.moneda(), "PEN"));
        producto.setUnidadMedida(dto.unidadMedida());
        producto.setImagenUrl(normalizarImagenUrl(dto.imagenUrl()));
        producto.setStockMinimoAlerta(dto.stockMinimoAlerta());
        producto.setTipo(dto.tipo());
        producto.setMarca(dto.marca());
        producto.setCantidad(dto.cantidad());
        // /api/productos es SOLO para productos vendibles
        producto.setEsInsumo(false);

        if (nuevaCat != null) {
            producto.setCategoria(nuevaCat);
            if (usuario.getSede() == null && nuevaCat.getSector() != null) {
                producto.setSector(nuevaCat.getSector());
            }
        } else {
            producto.setCategoria(null);
        }

        Producto guardado = repository.save(producto);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no existe"));
        verificarAccesoSector(producto.getSector());
        producto.setActivo(false);
        repository.save(producto);
    }

    private void verificarAccesoSector(Sector sectorEntidad) {
        Long sectorIdUsuario = obtenerSectorIdAutenticado();
        if (sectorIdUsuario != null && sectorEntidad != null
                && !sectorIdUsuario.equals(sectorEntidad.getId())) {
            throw new BusinessException("No tiene acceso a este recurso");
        }
    }

    private Long obtenerSectorIdAutenticado() {
        Usuario usuario = obtenerUsuarioAutenticado();
        return usuario.getSede() != null ? usuario.getSede().getId() : null;
    }

    private Usuario obtenerUsuarioAutenticado() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario autenticado no encontrado"));
    }

    /** Quita espacios y deja null si viene vacío (evita URLs rotas por espacios al pegar). */
    private static String normalizarImagenUrl(String url) {
        if (url == null) return null;
        String t = url.trim();
        return t.isEmpty() ? null : t;
    }

    private ProductoResponseDTO toResponseDTO(Producto p) {
        return new ProductoResponseDTO(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getPrecioCompra(),
                p.getStock(),
                p.getCategoria() != null ? p.getCategoria().getId() : null,
                p.getCategoria() != null ? p.getCategoria().getNombre() : null,
                p.getMoneda(),
                p.getUnidadMedida(),
                p.getImagenUrl(),
                p.getStockMinimoAlerta(),
                p.getTipo(),
                p.getMarca(),
                p.getCantidad(),
                p.getActivo(),
                p.getEsInsumo()
        );
    }
}
