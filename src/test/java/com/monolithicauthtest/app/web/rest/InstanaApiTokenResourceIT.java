package com.monolithicauthtest.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.monolithicauthtest.app.IntegrationTest;
import com.monolithicauthtest.app.domain.InstanaApiToken;
import com.monolithicauthtest.app.repository.InstanaApiTokenRepository;
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
 * Integration tests for the {@link InstanaApiTokenResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class InstanaApiTokenResourceIT {

    private static final String DEFAULT_TOKEN = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/instana-api-tokens";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private InstanaApiTokenRepository instanaApiTokenRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restInstanaApiTokenMockMvc;

    private InstanaApiToken instanaApiToken;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InstanaApiToken createEntity(EntityManager em) {
        InstanaApiToken instanaApiToken = new InstanaApiToken().token(DEFAULT_TOKEN).url(DEFAULT_URL);
        return instanaApiToken;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InstanaApiToken createUpdatedEntity(EntityManager em) {
        InstanaApiToken instanaApiToken = new InstanaApiToken().token(UPDATED_TOKEN).url(UPDATED_URL);
        return instanaApiToken;
    }

    @BeforeEach
    public void initTest() {
        instanaApiToken = createEntity(em);
    }

    @Test
    @Transactional
    void createInstanaApiToken() throws Exception {
        int databaseSizeBeforeCreate = instanaApiTokenRepository.findAll().size();
        // Create the InstanaApiToken
        restInstanaApiTokenMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isCreated());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeCreate + 1);
        InstanaApiToken testInstanaApiToken = instanaApiTokenList.get(instanaApiTokenList.size() - 1);
        assertThat(testInstanaApiToken.getToken()).isEqualTo(DEFAULT_TOKEN);
        assertThat(testInstanaApiToken.getUrl()).isEqualTo(DEFAULT_URL);
    }

    @Test
    @Transactional
    void createInstanaApiTokenWithExistingId() throws Exception {
        // Create the InstanaApiToken with an existing ID
        instanaApiToken.setId(1L);

        int databaseSizeBeforeCreate = instanaApiTokenRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restInstanaApiTokenMockMvc
            .perform(
                post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isBadRequest());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllInstanaApiTokens() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        // Get all the instanaApiTokenList
        restInstanaApiTokenMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(instanaApiToken.getId().intValue())))
            .andExpect(jsonPath("$.[*].token").value(hasItem(DEFAULT_TOKEN)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));
    }

    @Test
    @Transactional
    void getInstanaApiToken() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        // Get the instanaApiToken
        restInstanaApiTokenMockMvc
            .perform(get(ENTITY_API_URL_ID, instanaApiToken.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(instanaApiToken.getId().intValue()))
            .andExpect(jsonPath("$.token").value(DEFAULT_TOKEN))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL));
    }

    @Test
    @Transactional
    void getNonExistingInstanaApiToken() throws Exception {
        // Get the instanaApiToken
        restInstanaApiTokenMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingInstanaApiToken() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();

        // Update the instanaApiToken
        InstanaApiToken updatedInstanaApiToken = instanaApiTokenRepository.findById(instanaApiToken.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedInstanaApiToken are not directly saved in db
        em.detach(updatedInstanaApiToken);
        updatedInstanaApiToken.token(UPDATED_TOKEN).url(UPDATED_URL);

        restInstanaApiTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedInstanaApiToken.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedInstanaApiToken))
            )
            .andExpect(status().isOk());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
        InstanaApiToken testInstanaApiToken = instanaApiTokenList.get(instanaApiTokenList.size() - 1);
        assertThat(testInstanaApiToken.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testInstanaApiToken.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void putNonExistingInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, instanaApiToken.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isBadRequest());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isBadRequest());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateInstanaApiTokenWithPatch() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();

        // Update the instanaApiToken using partial update
        InstanaApiToken partialUpdatedInstanaApiToken = new InstanaApiToken();
        partialUpdatedInstanaApiToken.setId(instanaApiToken.getId());

        partialUpdatedInstanaApiToken.token(UPDATED_TOKEN).url(UPDATED_URL);

        restInstanaApiTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstanaApiToken.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstanaApiToken))
            )
            .andExpect(status().isOk());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
        InstanaApiToken testInstanaApiToken = instanaApiTokenList.get(instanaApiTokenList.size() - 1);
        assertThat(testInstanaApiToken.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testInstanaApiToken.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void fullUpdateInstanaApiTokenWithPatch() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();

        // Update the instanaApiToken using partial update
        InstanaApiToken partialUpdatedInstanaApiToken = new InstanaApiToken();
        partialUpdatedInstanaApiToken.setId(instanaApiToken.getId());

        partialUpdatedInstanaApiToken.token(UPDATED_TOKEN).url(UPDATED_URL);

        restInstanaApiTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedInstanaApiToken.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedInstanaApiToken))
            )
            .andExpect(status().isOk());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
        InstanaApiToken testInstanaApiToken = instanaApiTokenList.get(instanaApiTokenList.size() - 1);
        assertThat(testInstanaApiToken.getToken()).isEqualTo(UPDATED_TOKEN);
        assertThat(testInstanaApiToken.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void patchNonExistingInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, instanaApiToken.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isBadRequest());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isBadRequest());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamInstanaApiToken() throws Exception {
        int databaseSizeBeforeUpdate = instanaApiTokenRepository.findAll().size();
        instanaApiToken.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restInstanaApiTokenMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(instanaApiToken))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the InstanaApiToken in the database
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteInstanaApiToken() throws Exception {
        // Initialize the database
        instanaApiTokenRepository.saveAndFlush(instanaApiToken);

        int databaseSizeBeforeDelete = instanaApiTokenRepository.findAll().size();

        // Delete the instanaApiToken
        restInstanaApiTokenMockMvc
            .perform(delete(ENTITY_API_URL_ID, instanaApiToken.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<InstanaApiToken> instanaApiTokenList = instanaApiTokenRepository.findAll();
        assertThat(instanaApiTokenList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
