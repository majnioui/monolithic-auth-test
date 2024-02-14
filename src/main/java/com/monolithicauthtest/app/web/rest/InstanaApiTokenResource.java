package com.monolithicauthtest.app.web.rest;

import com.monolithicauthtest.app.domain.InstanaApiToken;
import com.monolithicauthtest.app.repository.InstanaApiTokenRepository;
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
 * REST controller for managing {@link com.monolithicauthtest.app.domain.InstanaApiToken}.
 */
@RestController
@RequestMapping("/api/instana-api-tokens")
@Transactional
public class InstanaApiTokenResource {

    private final Logger log = LoggerFactory.getLogger(InstanaApiTokenResource.class);

    private static final String ENTITY_NAME = "instanaApiToken";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InstanaApiTokenRepository instanaApiTokenRepository;

    public InstanaApiTokenResource(InstanaApiTokenRepository instanaApiTokenRepository) {
        this.instanaApiTokenRepository = instanaApiTokenRepository;
    }

    /**
     * {@code POST  /instana-api-tokens} : Create a new instanaApiToken.
     *
     * @param instanaApiToken the instanaApiToken to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new instanaApiToken, or with status {@code 400 (Bad Request)} if the instanaApiToken has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<InstanaApiToken> createInstanaApiToken(@RequestBody InstanaApiToken instanaApiToken) throws URISyntaxException {
        log.debug("REST request to save InstanaApiToken : {}", instanaApiToken);
        if (instanaApiToken.getId() != null) {
            throw new BadRequestAlertException("A new instanaApiToken cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InstanaApiToken result = instanaApiTokenRepository.save(instanaApiToken);
        return ResponseEntity
            .created(new URI("/api/instana-api-tokens/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /instana-api-tokens/:id} : Updates an existing instanaApiToken.
     *
     * @param id the id of the instanaApiToken to save.
     * @param instanaApiToken the instanaApiToken to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instanaApiToken,
     * or with status {@code 400 (Bad Request)} if the instanaApiToken is not valid,
     * or with status {@code 500 (Internal Server Error)} if the instanaApiToken couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InstanaApiToken> updateInstanaApiToken(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InstanaApiToken instanaApiToken
    ) throws URISyntaxException {
        log.debug("REST request to update InstanaApiToken : {}, {}", id, instanaApiToken);
        if (instanaApiToken.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instanaApiToken.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanaApiTokenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InstanaApiToken result = instanaApiTokenRepository.save(instanaApiToken);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instanaApiToken.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /instana-api-tokens/:id} : Partial updates given fields of an existing instanaApiToken, field will ignore if it is null
     *
     * @param id the id of the instanaApiToken to save.
     * @param instanaApiToken the instanaApiToken to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated instanaApiToken,
     * or with status {@code 400 (Bad Request)} if the instanaApiToken is not valid,
     * or with status {@code 404 (Not Found)} if the instanaApiToken is not found,
     * or with status {@code 500 (Internal Server Error)} if the instanaApiToken couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InstanaApiToken> partialUpdateInstanaApiToken(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InstanaApiToken instanaApiToken
    ) throws URISyntaxException {
        log.debug("REST request to partial update InstanaApiToken partially : {}, {}", id, instanaApiToken);
        if (instanaApiToken.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, instanaApiToken.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!instanaApiTokenRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InstanaApiToken> result = instanaApiTokenRepository
            .findById(instanaApiToken.getId())
            .map(existingInstanaApiToken -> {
                if (instanaApiToken.getToken() != null) {
                    existingInstanaApiToken.setToken(instanaApiToken.getToken());
                }

                return existingInstanaApiToken;
            })
            .map(instanaApiTokenRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, instanaApiToken.getId().toString())
        );
    }

    /**
     * {@code GET  /instana-api-tokens} : get all the instanaApiTokens.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of instanaApiTokens in body.
     */
    @GetMapping("")
    public List<InstanaApiToken> getAllInstanaApiTokens() {
        log.debug("REST request to get all InstanaApiTokens");
        return instanaApiTokenRepository.findAll();
    }

    /**
     * {@code GET  /instana-api-tokens/:id} : get the "id" instanaApiToken.
     *
     * @param id the id of the instanaApiToken to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the instanaApiToken, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstanaApiToken> getInstanaApiToken(@PathVariable Long id) {
        log.debug("REST request to get InstanaApiToken : {}", id);
        Optional<InstanaApiToken> instanaApiToken = instanaApiTokenRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(instanaApiToken);
    }

    /**
     * {@code DELETE  /instana-api-tokens/:id} : delete the "id" instanaApiToken.
     *
     * @param id the id of the instanaApiToken to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstanaApiToken(@PathVariable Long id) {
        log.debug("REST request to delete InstanaApiToken : {}", id);
        instanaApiTokenRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
