package com.monolithicauthtest.app.repository;

import com.monolithicauthtest.app.domain.Gitrep;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Gitrep entity.
 */
@SuppressWarnings("unused")
@Repository
public interface GitrepRepository extends JpaRepository<Gitrep, Long> {}
