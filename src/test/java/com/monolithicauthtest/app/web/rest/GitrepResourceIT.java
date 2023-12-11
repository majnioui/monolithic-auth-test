package com.monolithicauthtest.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.monolithicauthtest.app.IntegrationTest;
import com.monolithicauthtest.app.domain.Gitrep;
import com.monolithicauthtest.app.repository.GitrepRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link GitrepResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class GitrepResourceIT {

    private static final String DEFAULT_CLIENTID = "AAAAAAAAAA";
    private static final String UPDATED_CLIENTID = "BBBBBBBBBB";

    private static final String DEFAULT_ACCESSTOKEN = "AAAAAAAAAA";
    private static final String UPDATED_ACCESSTOKEN = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/gitreps";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private GitrepRepository gitrepRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restGitrepMockMvc;

    private Gitrep gitrep;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Gitrep createEntity(EntityManager em) {
        Gitrep gitrep = new Gitrep().clientid(DEFAULT_CLIENTID).accesstoken(DEFAULT_ACCESSTOKEN);
        return gitrep;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Gitrep createUpdatedEntity(EntityManager em) {
        Gitrep gitrep = new Gitrep().clientid(UPDATED_CLIENTID).accesstoken(UPDATED_ACCESSTOKEN);
        return gitrep;
    }

    @BeforeEach
    public void initTest() {
        gitrep = createEntity(em);
    }

    @Test
    @Transactional
    void createGitrep() throws Exception {
        int databaseSizeBeforeCreate = gitrepRepository.findAll().size();
        // Create the Gitrep
        restGitrepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gitrep)))
            .andExpect(status().isCreated());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeCreate + 1);
        Gitrep testGitrep = gitrepList.get(gitrepList.size() - 1);
        assertThat(testGitrep.getClientid()).isEqualTo(DEFAULT_CLIENTID);
        assertThat(testGitrep.getAccesstoken()).isEqualTo(DEFAULT_ACCESSTOKEN);
    }

    @Test
    @Transactional
    void createGitrepWithExistingId() throws Exception {
        // Create the Gitrep with an existing ID
        gitrep.setId(1L);

        int databaseSizeBeforeCreate = gitrepRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restGitrepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gitrep)))
            .andExpect(status().isBadRequest());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkAccesstokenIsRequired() throws Exception {
        int databaseSizeBeforeTest = gitrepRepository.findAll().size();
        // set the field null
        gitrep.setAccesstoken(null);

        // Create the Gitrep, which fails.

        restGitrepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gitrep)))
            .andExpect(status().isBadRequest());

        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllGitreps() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        // Get all the gitrepList
        restGitrepMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(gitrep.getId().intValue())))
            .andExpect(jsonPath("$.[*].clientid").value(hasItem(DEFAULT_CLIENTID)))
            .andExpect(jsonPath("$.[*].accesstoken").value(hasItem(DEFAULT_ACCESSTOKEN)));
    }

    @Test
    @Transactional
    void getGitrep() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        // Get the gitrep
        restGitrepMockMvc
            .perform(get(ENTITY_API_URL_ID, gitrep.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(gitrep.getId().intValue()))
            .andExpect(jsonPath("$.clientid").value(DEFAULT_CLIENTID))
            .andExpect(jsonPath("$.accesstoken").value(DEFAULT_ACCESSTOKEN));
    }

    @Test
    @Transactional
    void getNonExistingGitrep() throws Exception {
        // Get the gitrep
        restGitrepMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingGitrep() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();

        // Update the gitrep
        Gitrep updatedGitrep = gitrepRepository.findById(gitrep.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedGitrep are not directly saved in db
        em.detach(updatedGitrep);
        updatedGitrep.clientid(UPDATED_CLIENTID).accesstoken(UPDATED_ACCESSTOKEN);

        restGitrepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedGitrep.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedGitrep))
            )
            .andExpect(status().isOk());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
        Gitrep testGitrep = gitrepList.get(gitrepList.size() - 1);
        assertThat(testGitrep.getClientid()).isEqualTo(UPDATED_CLIENTID);
        assertThat(testGitrep.getAccesstoken()).isEqualTo(UPDATED_ACCESSTOKEN);
    }

    @Test
    @Transactional
    void putNonExistingGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, gitrep.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gitrep))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(gitrep))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(gitrep)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateGitrepWithPatch() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();

        // Update the gitrep using partial update
        Gitrep partialUpdatedGitrep = new Gitrep();
        partialUpdatedGitrep.setId(gitrep.getId());

        partialUpdatedGitrep.clientid(UPDATED_CLIENTID);

        restGitrepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGitrep.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGitrep))
            )
            .andExpect(status().isOk());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
        Gitrep testGitrep = gitrepList.get(gitrepList.size() - 1);
        assertThat(testGitrep.getClientid()).isEqualTo(UPDATED_CLIENTID);
        assertThat(testGitrep.getAccesstoken()).isEqualTo(DEFAULT_ACCESSTOKEN);
    }

    @Test
    @Transactional
    void fullUpdateGitrepWithPatch() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();

        // Update the gitrep using partial update
        Gitrep partialUpdatedGitrep = new Gitrep();
        partialUpdatedGitrep.setId(gitrep.getId());

        partialUpdatedGitrep.clientid(UPDATED_CLIENTID).accesstoken(UPDATED_ACCESSTOKEN);

        restGitrepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedGitrep.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedGitrep))
            )
            .andExpect(status().isOk());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
        Gitrep testGitrep = gitrepList.get(gitrepList.size() - 1);
        assertThat(testGitrep.getClientid()).isEqualTo(UPDATED_CLIENTID);
        assertThat(testGitrep.getAccesstoken()).isEqualTo(UPDATED_ACCESSTOKEN);
    }

    @Test
    @Transactional
    void patchNonExistingGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, gitrep.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gitrep))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(gitrep))
            )
            .andExpect(status().isBadRequest());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamGitrep() throws Exception {
        int databaseSizeBeforeUpdate = gitrepRepository.findAll().size();
        gitrep.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restGitrepMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(gitrep)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Gitrep in the database
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteGitrep() throws Exception {
        // Initialize the database
        gitrepRepository.saveAndFlush(gitrep);

        int databaseSizeBeforeDelete = gitrepRepository.findAll().size();

        // Delete the gitrep
        restGitrepMockMvc
            .perform(delete(ENTITY_API_URL_ID, gitrep.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Gitrep> gitrepList = gitrepRepository.findAll();
        assertThat(gitrepList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
