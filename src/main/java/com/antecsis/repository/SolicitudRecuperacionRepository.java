package com.antecsis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Sector;
import com.antecsis.entity.SolicitudRecuperacionContrasena;

public interface SolicitudRecuperacionRepository extends JpaRepository<SolicitudRecuperacionContrasena, Long> {
    List<SolicitudRecuperacionContrasena> findByUsuario_SedeAndEstadoOrderByFechaSolicitudDesc(
            Sector sede, SolicitudRecuperacionContrasena.Estado estado);
    List<SolicitudRecuperacionContrasena> findByEstadoOrderByFechaSolicitudDesc(
            SolicitudRecuperacionContrasena.Estado estado);
    void deleteByUsuario_IdAndEstado(Long usuarioId, SolicitudRecuperacionContrasena.Estado estado);
    void deleteByUsuario_Id(Long usuarioId);
}
