package com.monolithicauthtest.app.domain;

import static com.monolithicauthtest.app.domain.ClientTestSamples.*;
import static com.monolithicauthtest.app.domain.GitrepTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.monolithicauthtest.app.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ClientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Client.class);
        Client client1 = getClientSample1();
        Client client2 = new Client();
        assertThat(client1).isNotEqualTo(client2);

        client2.setId(client1.getId());
        assertThat(client1).isEqualTo(client2);

        client2 = getClientSample2();
        assertThat(client1).isNotEqualTo(client2);
    }

    @Test
    void gitrepTest() throws Exception {
        Client client = getClientRandomSampleGenerator();
        Gitrep gitrepBack = getGitrepRandomSampleGenerator();

        client.addGitrep(gitrepBack);
        assertThat(client.getGitreps()).containsOnly(gitrepBack);
        assertThat(gitrepBack.getClient()).isEqualTo(client);

        client.removeGitrep(gitrepBack);
        assertThat(client.getGitreps()).doesNotContain(gitrepBack);
        assertThat(gitrepBack.getClient()).isNull();

        client.gitreps(new HashSet<>(Set.of(gitrepBack)));
        assertThat(client.getGitreps()).containsOnly(gitrepBack);
        assertThat(gitrepBack.getClient()).isEqualTo(client);

        client.setGitreps(new HashSet<>());
        assertThat(client.getGitreps()).doesNotContain(gitrepBack);
        assertThat(gitrepBack.getClient()).isNull();
    }
}
