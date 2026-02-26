package com.antecsis.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.antecsis.entity.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByCodigo(String codigo);

    List<Modulo> findByActivoTrueOrderByOrdenAsc();

    List<Modulo> findByCodigoInAndActivoTrue(Set<String> codigos);
}
