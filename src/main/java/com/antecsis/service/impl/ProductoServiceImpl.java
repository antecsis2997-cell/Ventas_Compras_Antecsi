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
        Long sectorId = usuario.getSede() != null ? usuario.getSede().getId() : null;

        // Validar duplicados por nombre dentro del mismo sector
        if (sectorId != null && dto.nombre() != null) {
            if (repository.existsByNombreAndSectorId(dto.nombre(), sectorId)) {
                throw new BusinessException("Ya existe un producto con ese nombre en tu bodega");
            }
        }

        // Validar duplicados por código dentro del mismo sector
        if (sectorId != null && dto.codigo() != null && !dto.codigo().isBlank()) {
            if (repository.existsByCodigoAndSectorId(dto.codigo(), sectorId)) {
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
        producto.setImagenUrl(dto.imagenUrl());
        producto.setStockMinimoAlerta(dto.stockMinimoAlerta());
        producto.setTipo(dto.tipo());
        producto.setMarca(dto.marca());
        producto.setCantidad(dto.cantidad());
        producto.setSector(usuario.getSede());
        producto.setActivo(true);

        if (dto.categoriaId() != null) {
            Categoria cat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new BusinessException("Categoría no existe"));
            verificarAccesoSector(cat.getSector());
            producto.setCategoria(cat);
        }

        Producto guardado = repository.save(producto);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listar(Pageable pageable) {
        Long sectorId = obtenerSectorIdAutenticado();
        if (sectorId != null) {
            return repository.findBySectorId(sectorId, pageable).map(this::toResponseDTO);
        }
        return repository.findAll(pageable).map(this::toResponseDTO);
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
        Long sectorId = usuario.getSede() != null ? usuario.getSede().getId() : null;

        // Validar duplicados por nombre (excluyendo el producto actual)
        if (sectorId != null && dto.nombre() != null) {
            boolean existeOtroConMismoNombre = repository.findBySectorId(sectorId, Pageable.unpaged())
                .stream()
                .anyMatch(p -> !p.getId().equals(id) && p.getNombre().equals(dto.nombre()));
            if (existeOtroConMismoNombre) {
                throw new BusinessException("Ya existe otro producto con ese nombre en tu bodega");
            }
        }

        // Validar duplicados por código (excluyendo el producto actual)
        if (sectorId != null && dto.codigo() != null && !dto.codigo().isBlank()) {
            boolean existeOtroConMismoCodigo = repository.findBySectorId(sectorId, Pageable.unpaged())
                .stream()
                .anyMatch(p -> !p.getId().equals(id) && dto.codigo().equals(p.getCodigo()));
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
        producto.setImagenUrl(dto.imagenUrl());
        producto.setStockMinimoAlerta(dto.stockMinimoAlerta());
        producto.setTipo(dto.tipo());
        producto.setMarca(dto.marca());
        producto.setCantidad(dto.cantidad());

        if (dto.categoriaId() != null) {
            Categoria cat = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new BusinessException("Categoría no existe"));
            verificarAccesoSector(cat.getSector());
            producto.setCategoria(cat);
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
                p.getActivo()
        );
    }
}
