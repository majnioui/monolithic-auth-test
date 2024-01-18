package com.monolithicauthtest.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DockerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Docker getDockerSample1() {
        return new Docker().id(1L).username("username1").repoName("repoName1").url("url1");
    }

    public static Docker getDockerSample2() {
        return new Docker().id(2L).username("username2").repoName("repoName2").url("url2");
    }

    public static Docker getDockerRandomSampleGenerator() {
        return new Docker()
            .id(longCount.incrementAndGet())
            .username(UUID.randomUUID().toString())
            .repoName(UUID.randomUUID().toString())
            .url(UUID.randomUUID().toString());
    }
}
