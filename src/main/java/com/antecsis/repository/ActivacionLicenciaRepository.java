package com.antecsis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.ActivacionLicencia;

public interface ActivacionLicenciaRepository extends JpaRepository<ActivacionLicencia, Long> {
    Optional<ActivacionLicencia> findBySuscripcion_Id(Long suscripcionId);
}
