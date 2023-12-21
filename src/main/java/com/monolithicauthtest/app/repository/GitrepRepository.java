package com.monolithicauthtest.app.repository;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.domain.Gitrep.PlatformType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Gitrep entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GitrepRepository extends JpaRepository<Gitrep, Long> {
    Optional<Gitrep> findFirstByOrderByCreatedAtDesc();
    Optional<Gitrep> findFirstByPlatformTypeOrderByCreatedAtDesc(PlatformType platformType);

    void deleteByClientid(String clientid);

    @Modifying
    @Query("DELETE FROM Gitrep g WHERE g.clientid = :clientId AND g.platformType = :platformType")
    void deleteByClientidAndPlatformType(@Param("clientId") String clientId, @Param("platformType") Gitrep.PlatformType platformType);

    Optional<Gitrep> findByClientid(String clientId);
    Optional<Gitrep> findByClientidAndPlatformType(String clientId, Gitrep.PlatformType platformType);
}
