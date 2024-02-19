package com.monolithicauthtest.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class InstanaApiTokenTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static InstanaApiToken getInstanaApiTokenSample1() {
        return new InstanaApiToken().id(1L).token("token1").url("url1");
    }

    public static InstanaApiToken getInstanaApiTokenSample2() {
        return new InstanaApiToken().id(2L).token("token2").url("url2");
    }

    public static InstanaApiToken getInstanaApiTokenRandomSampleGenerator() {
        return new InstanaApiToken().id(longCount.incrementAndGet()).token(UUID.randomUUID().toString()).url(UUID.randomUUID().toString());
    }
}
