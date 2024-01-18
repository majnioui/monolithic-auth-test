package com.monolithicauthtest.app.domain;

import static com.monolithicauthtest.app.domain.DockerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.monolithicauthtest.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DockerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Docker.class);
        Docker docker1 = getDockerSample1();
        Docker docker2 = new Docker();
        assertThat(docker1).isNotEqualTo(docker2);

        docker2.setId(docker1.getId());
        assertThat(docker1).isEqualTo(docker2);

        docker2 = getDockerSample2();
        assertThat(docker1).isNotEqualTo(docker2);
    }
}
