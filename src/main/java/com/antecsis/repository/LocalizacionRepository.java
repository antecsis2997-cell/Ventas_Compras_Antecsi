package com.antecsis.repository;

import com.antecsis.entity.Localizacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalizacionRepository extends JpaRepository<Localizacion, Long> {
}
