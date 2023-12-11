package com.monolithicauthtest.app.domain;

import static com.monolithicauthtest.app.domain.ClientTestSamples.*;
import static com.monolithicauthtest.app.domain.GitrepTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.monolithicauthtest.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class GitrepTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Gitrep.class);
        Gitrep gitrep1 = getGitrepSample1();
        Gitrep gitrep2 = new Gitrep();
        assertThat(gitrep1).isNotEqualTo(gitrep2);

        gitrep2.setId(gitrep1.getId());
        assertThat(gitrep1).isEqualTo(gitrep2);

        gitrep2 = getGitrepSample2();
        assertThat(gitrep1).isNotEqualTo(gitrep2);
    }

    @Test
    void clientTest() throws Exception {
        Gitrep gitrep = getGitrepRandomSampleGenerator();
        Client clientBack = getClientRandomSampleGenerator();

        gitrep.setClient(clientBack);
        assertThat(gitrep.getClient()).isEqualTo(clientBack);

        gitrep.client(null);
        assertThat(gitrep.getClient()).isNull();
    }
}
