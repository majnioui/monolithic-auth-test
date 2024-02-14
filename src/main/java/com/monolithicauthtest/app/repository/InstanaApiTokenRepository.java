package com.monolithicauthtest.app.repository;

import com.monolithicauthtest.app.domain.InstanaApiToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the InstanaApiToken entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InstanaApiTokenRepository extends JpaRepository<InstanaApiToken, Long> {
    InstanaApiToken findTopByOrderByIdDesc();
}
