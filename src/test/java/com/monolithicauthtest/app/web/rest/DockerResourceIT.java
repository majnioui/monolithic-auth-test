package com.monolithicauthtest.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.monolithicauthtest.app.IntegrationTest;
import com.monolithicauthtest.app.domain.Docker;
import com.monolithicauthtest.app.repository.DockerRepository;
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
 * Integration tests for the {@link DockerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DockerResourceIT {

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_REPO_NAME = "AAAAAAAAAA";
    private static final String UPDATED_REPO_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_URL = "AAAAAAAAAA";
    private static final String UPDATED_URL = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dockers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DockerRepository dockerRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDockerMockMvc;

    private Docker docker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Docker createEntity(EntityManager em) {
        Docker docker = new Docker().username(DEFAULT_USERNAME).repoName(DEFAULT_REPO_NAME).url(DEFAULT_URL);
        return docker;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Docker createUpdatedEntity(EntityManager em) {
        Docker docker = new Docker().username(UPDATED_USERNAME).repoName(UPDATED_REPO_NAME).url(UPDATED_URL);
        return docker;
    }

    @BeforeEach
    public void initTest() {
        docker = createEntity(em);
    }

    @Test
    @Transactional
    void createDocker() throws Exception {
        int databaseSizeBeforeCreate = dockerRepository.findAll().size();
        // Create the Docker
        restDockerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(docker)))
            .andExpect(status().isCreated());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeCreate + 1);
        Docker testDocker = dockerList.get(dockerList.size() - 1);
        assertThat(testDocker.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(testDocker.getRepoName()).isEqualTo(DEFAULT_REPO_NAME);
        assertThat(testDocker.getUrl()).isEqualTo(DEFAULT_URL);
    }

    @Test
    @Transactional
    void createDockerWithExistingId() throws Exception {
        // Create the Docker with an existing ID
        docker.setId(1L);

        int databaseSizeBeforeCreate = dockerRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDockerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(docker)))
            .andExpect(status().isBadRequest());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllDockers() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        // Get all the dockerList
        restDockerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(docker.getId().intValue())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].repoName").value(hasItem(DEFAULT_REPO_NAME)))
            .andExpect(jsonPath("$.[*].url").value(hasItem(DEFAULT_URL)));
    }

    @Test
    @Transactional
    void getDocker() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        // Get the docker
        restDockerMockMvc
            .perform(get(ENTITY_API_URL_ID, docker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(docker.getId().intValue()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.repoName").value(DEFAULT_REPO_NAME))
            .andExpect(jsonPath("$.url").value(DEFAULT_URL));
    }

    @Test
    @Transactional
    void getNonExistingDocker() throws Exception {
        // Get the docker
        restDockerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDocker() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();

        // Update the docker
        Docker updatedDocker = dockerRepository.findById(docker.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDocker are not directly saved in db
        em.detach(updatedDocker);
        updatedDocker.username(UPDATED_USERNAME).repoName(UPDATED_REPO_NAME).url(UPDATED_URL);

        restDockerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDocker.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedDocker))
            )
            .andExpect(status().isOk());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
        Docker testDocker = dockerList.get(dockerList.size() - 1);
        assertThat(testDocker.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testDocker.getRepoName()).isEqualTo(UPDATED_REPO_NAME);
        assertThat(testDocker.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void putNonExistingDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, docker.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(docker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(docker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(docker)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDockerWithPatch() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();

        // Update the docker using partial update
        Docker partialUpdatedDocker = new Docker();
        partialUpdatedDocker.setId(docker.getId());

        partialUpdatedDocker.username(UPDATED_USERNAME).repoName(UPDATED_REPO_NAME).url(UPDATED_URL);

        restDockerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDocker))
            )
            .andExpect(status().isOk());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
        Docker testDocker = dockerList.get(dockerList.size() - 1);
        assertThat(testDocker.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testDocker.getRepoName()).isEqualTo(UPDATED_REPO_NAME);
        assertThat(testDocker.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void fullUpdateDockerWithPatch() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();

        // Update the docker using partial update
        Docker partialUpdatedDocker = new Docker();
        partialUpdatedDocker.setId(docker.getId());

        partialUpdatedDocker.username(UPDATED_USERNAME).repoName(UPDATED_REPO_NAME).url(UPDATED_URL);

        restDockerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDocker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDocker))
            )
            .andExpect(status().isOk());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
        Docker testDocker = dockerList.get(dockerList.size() - 1);
        assertThat(testDocker.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testDocker.getRepoName()).isEqualTo(UPDATED_REPO_NAME);
        assertThat(testDocker.getUrl()).isEqualTo(UPDATED_URL);
    }

    @Test
    @Transactional
    void patchNonExistingDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, docker.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(docker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(docker))
            )
            .andExpect(status().isBadRequest());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDocker() throws Exception {
        int databaseSizeBeforeUpdate = dockerRepository.findAll().size();
        docker.setId(longCount.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDockerMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(docker)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Docker in the database
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDocker() throws Exception {
        // Initialize the database
        dockerRepository.saveAndFlush(docker);

        int databaseSizeBeforeDelete = dockerRepository.findAll().size();

        // Delete the docker
        restDockerMockMvc
            .perform(delete(ENTITY_API_URL_ID, docker.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Docker> dockerList = dockerRepository.findAll();
        assertThat(dockerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
