package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Docker;
import com.monolithicauthtest.app.repository.DockerRepository;
import com.monolithicauthtest.app.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.monolithicauthtest.app.domain.Docker}.
 */
@RestController
@RequestMapping("/api/dockers")
@Transactional
public class DockerResource {

    private final Logger log = LoggerFactory.getLogger(DockerResource.class);

    private static final String ENTITY_NAME = "docker";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DockerRepository dockerRepository;

    public DockerResource(DockerRepository dockerRepository) {
        this.dockerRepository = dockerRepository;
    }

    /**
     * {@code POST  /dockers} : Create a new docker.
     *
     * @param docker the docker to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new docker, or with status {@code 400 (Bad Request)} if the docker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Docker> createDocker(@RequestBody Docker docker) throws URISyntaxException {
        log.debug("REST request to save Docker : {}", docker);
        if (docker.getId() != null) {
            throw new BadRequestAlertException("A new docker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Docker result = dockerRepository.save(docker);
        return ResponseEntity
            .created(new URI("/api/dockers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /dockers/:id} : Updates an existing docker.
     *
     * @param id the id of the docker to save.
     * @param docker the docker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated docker,
     * or with status {@code 400 (Bad Request)} if the docker is not valid,
     * or with status {@code 500 (Internal Server Error)} if the docker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Docker> updateDocker(@PathVariable(value = "id", required = false) final Long id, @RequestBody Docker docker)
        throws URISyntaxException {
        log.debug("REST request to update Docker : {}, {}", id, docker);
        if (docker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, docker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dockerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Docker result = dockerRepository.save(docker);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, docker.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /dockers/:id} : Partial updates given fields of an existing docker, field will ignore if it is null
     *
     * @param id the id of the docker to save.
     * @param docker the docker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated docker,
     * or with status {@code 400 (Bad Request)} if the docker is not valid,
     * or with status {@code 404 (Not Found)} if the docker is not found,
     * or with status {@code 500 (Internal Server Error)} if the docker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Docker> partialUpdateDocker(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Docker docker
    ) throws URISyntaxException {
        log.debug("REST request to partial update Docker partially : {}, {}", id, docker);
        if (docker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, docker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dockerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Docker> result = dockerRepository
            .findById(docker.getId())
            .map(existingDocker -> {
                if (docker.getUsername() != null) {
                    existingDocker.setUsername(docker.getUsername());
                }
                if (docker.getRepoName() != null) {
                    existingDocker.setRepoName(docker.getRepoName());
                }
                if (docker.getUrl() != null) {
                    existingDocker.setUrl(docker.getUrl());
                }

                return existingDocker;
            })
            .map(dockerRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, docker.getId().toString())
        );
    }

    /**
     * {@code GET  /dockers} : get all the dockers.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of dockers in body.
     */
    @GetMapping("")
    public List<Docker> getAllDockers() {
        log.debug("REST request to get all Dockers");
        return dockerRepository.findAll();
    }

    /**
     * {@code GET  /dockers/:id} : get the "id" docker.
     *
     * @param id the id of the docker to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the docker, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Docker> getDocker(@PathVariable Long id) {
        log.debug("REST request to get Docker : {}", id);
        Optional<Docker> docker = dockerRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(docker);
    }

    /**
     * {@code DELETE  /dockers/:id} : delete the "id" docker.
     *
     * @param id the id of the docker to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocker(@PathVariable Long id) {
        log.debug("REST request to delete Docker : {}", id);
        dockerRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
