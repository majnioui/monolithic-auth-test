package com.monolithicauthtest.app.domain;

import static com.monolithicauthtest.app.domain.InstanaApiTokenTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.monolithicauthtest.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class InstanaApiTokenTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(InstanaApiToken.class);
        InstanaApiToken instanaApiToken1 = getInstanaApiTokenSample1();
        InstanaApiToken instanaApiToken2 = new InstanaApiToken();
        assertThat(instanaApiToken1).isNotEqualTo(instanaApiToken2);

        instanaApiToken2.setId(instanaApiToken1.getId());
        assertThat(instanaApiToken1).isEqualTo(instanaApiToken2);

        instanaApiToken2 = getInstanaApiTokenSample2();
        assertThat(instanaApiToken1).isNotEqualTo(instanaApiToken2);
    }
}
