package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import com.monolithicauthtest.app.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
 * REST controller for managing {@link com.monolithicauthtest.app.domain.Gitrep}.
 */
@RestController
@RequestMapping("/api/gitreps")
@Transactional
public class GitrepResource {

    private final Logger log = LoggerFactory.getLogger(GitrepResource.class);

    private static final String ENTITY_NAME = "gitrep";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GitrepRepository gitrepRepository;

    public GitrepResource(GitrepRepository gitrepRepository) {
        this.gitrepRepository = gitrepRepository;
    }

    /**
     * {@code POST  /gitreps} : Create a new gitrep.
     *
     * @param gitrep the gitrep to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new gitrep, or with status {@code 400 (Bad Request)} if the gitrep has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Gitrep> createGitrep(@Valid @RequestBody Gitrep gitrep) throws URISyntaxException {
        log.debug("REST request to save Gitrep : {}", gitrep);
        if (gitrep.getId() != null) {
            throw new BadRequestAlertException("A new gitrep cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Gitrep result = gitrepRepository.save(gitrep);
        return ResponseEntity
            .created(new URI("/api/gitreps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /gitreps/:id} : Updates an existing gitrep.
     *
     * @param id the id of the gitrep to save.
     * @param gitrep the gitrep to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gitrep,
     * or with status {@code 400 (Bad Request)} if the gitrep is not valid,
     * or with status {@code 500 (Internal Server Error)} if the gitrep couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Gitrep> updateGitrep(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Gitrep gitrep
    ) throws URISyntaxException {
        log.debug("REST request to update Gitrep : {}, {}", id, gitrep);
        if (gitrep.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gitrep.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!gitrepRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Gitrep result = gitrepRepository.save(gitrep);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gitrep.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /gitreps/:id} : Partial updates given fields of an existing gitrep, field will ignore if it is null
     *
     * @param id the id of the gitrep to save.
     * @param gitrep the gitrep to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated gitrep,
     * or with status {@code 400 (Bad Request)} if the gitrep is not valid,
     * or with status {@code 404 (Not Found)} if the gitrep is not found,
     * or with status {@code 500 (Internal Server Error)} if the gitrep couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Gitrep> partialUpdateGitrep(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Gitrep gitrep
    ) throws URISyntaxException {
        log.debug("REST request to partial update Gitrep partially : {}, {}", id, gitrep);
        if (gitrep.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, gitrep.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!gitrepRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Gitrep> result = gitrepRepository
            .findById(gitrep.getId())
            .map(existingGitrep -> {
                if (gitrep.getClientid() != null) {
                    existingGitrep.setClientid(gitrep.getClientid());
                }
                if (gitrep.getAccesstoken() != null) {
                    existingGitrep.setAccesstoken(gitrep.getAccesstoken());
                }

                return existingGitrep;
            })
            .map(gitrepRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, gitrep.getId().toString())
        );
    }

    /**
     * {@code GET  /gitreps} : get all the gitreps.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of gitreps in body.
     */
    @GetMapping("")
    public List<Gitrep> getAllGitreps() {
        log.debug("REST request to get all Gitreps");
        return gitrepRepository.findAll();
    }

    /**
     * {@code GET  /gitreps/:id} : get the "id" gitrep.
     *
     * @param id the id of the gitrep to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the gitrep, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Gitrep> getGitrep(@PathVariable Long id) {
        log.debug("REST request to get Gitrep : {}", id);
        Optional<Gitrep> gitrep = gitrepRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(gitrep);
    }

    /**
     * {@code DELETE  /gitreps/:id} : delete the "id" gitrep.
     *
     * @param id the id of the gitrep to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGitrep(@PathVariable Long id) {
        log.debug("REST request to delete Gitrep : {}", id);
        gitrepRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
