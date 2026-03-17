package com.antecsis.repository;

import com.antecsis.entity.SecuenciaComprobante;
import com.antecsis.entity.TipoDocumentoVenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SecuenciaComprobanteRepository extends JpaRepository<SecuenciaComprobante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SecuenciaComprobante s WHERE s.sector.id = :sectorId AND s.tipoDocumento = :tipo")
    Optional<SecuenciaComprobante> findBySectorIdAndTipoDocumentoForUpdate(
            @Param("sectorId") Long sectorId,
            @Param("tipo") TipoDocumentoVenta tipo);

    Optional<SecuenciaComprobante> findBySectorIdAndTipoDocumento(Long sectorId, TipoDocumentoVenta tipo);
}
