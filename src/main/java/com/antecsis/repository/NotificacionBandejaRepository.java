package com.antecsis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.NotificacionBandeja;

public interface NotificacionBandejaRepository extends JpaRepository<NotificacionBandeja, Long> {
    List<NotificacionBandeja> findByEmailDestinoIgnoreCaseOrderByCreatedAtDesc(String emailDestino);
}
