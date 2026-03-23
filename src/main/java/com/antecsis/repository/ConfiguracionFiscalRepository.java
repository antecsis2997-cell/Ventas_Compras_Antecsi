package com.antecsis.repository;

import com.antecsis.entity.ConfiguracionFiscal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionFiscalRepository extends JpaRepository<ConfiguracionFiscal, Long> {

    Optional<ConfiguracionFiscal> findBySectorId(Long sectorId);

    Optional<ConfiguracionFiscal> findBySectorIdAndActivoTrue(Long sectorId);
}
