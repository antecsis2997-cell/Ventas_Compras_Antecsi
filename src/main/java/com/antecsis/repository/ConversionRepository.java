package com.antecsis.repository;

import com.antecsis.entity.Conversion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversionRepository extends JpaRepository<Conversion, Long> {
    Page<Conversion> findBySectorId(Long sectorId, Pageable pageable);
}

