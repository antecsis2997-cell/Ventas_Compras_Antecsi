package com.antecsis.service.impl;

import com.antecsis.dto.mensaje.MensajeRequestDTO;
import com.antecsis.dto.mensaje.MensajeResponseDTO;
import com.antecsis.entity.Mensaje;
import com.antecsis.exception.BusinessException;
import com.antecsis.repository.MensajeRepository;
import com.antecsis.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MensajeServiceImpl implements MensajeService {

    private final MensajeRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MensajeResponseDTO crear(MensajeRequestDTO dto) {
        Mensaje m = new Mensaje();
        m.setNombreReceptor(dto.getNombreReceptor());
        m.setItem(dto.getItem());
        m.setDescripcion(dto.getDescripcion());
        m.setPrecio(dto.getPrecio());
        m.setEstado(dto.getEstado());
        m.setNombreEmisor(dto.getNombreEmisor());
        Mensaje guardado = repository.save(m);
        MensajeResponseDTO response = toDTO(guardado);
        messagingTemplate.convertAndSend("/topic/mensajes", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MensajeResponseDTO> listar(Pageable pageable) {
        return repository.findByOrderByFechaDesc(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MensajeResponseDTO> listarPorReceptor(String nombreReceptor, Pageable pageable) {
        return repository.findByNombreReceptorOrderByFechaDesc(nombreReceptor, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MensajeResponseDTO obtenerPorId(Long id) {
        Mensaje m = repository.findById(id).orElseThrow(() -> new BusinessException("Mensaje no existe"));
        return toDTO(m);
    }

    private MensajeResponseDTO toDTO(Mensaje m) {
        return new MensajeResponseDTO(
                m.getId(),
                m.getNombreReceptor(),
                m.getItem(),
                m.getDescripcion(),
                m.getPrecio(),
                m.getEstado() != null ? m.getEstado().name() : null,
                m.getFecha(),
                m.getNombreEmisor()
        );
    }
}
