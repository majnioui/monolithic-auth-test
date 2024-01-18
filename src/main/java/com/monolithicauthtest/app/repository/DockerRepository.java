package com.monolithicauthtest.app.repository;

import com.monolithicauthtest.app.domain.Docker;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Docker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DockerRepository extends JpaRepository<Docker, Long> {}
