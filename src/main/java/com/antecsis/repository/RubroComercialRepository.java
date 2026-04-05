package com.antecsis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.RubroComercial;

public interface RubroComercialRepository extends JpaRepository<RubroComercial, Long> {
    Optional<RubroComercial> findByCodigoIgnoreCase(String codigo);

    List<RubroComercial> findByActivoTrueOrderByOrdenAsc();

    List<RubroComercial> findAllByOrderByOrdenAsc();
}
