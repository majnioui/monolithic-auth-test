package com.monolithicauthtest.app.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class GitrepTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Gitrep getGitrepSample1() {
        return new Gitrep().id(1L).clientid("clientid1").accesstoken("accesstoken1");
    }

    public static Gitrep getGitrepSample2() {
        return new Gitrep().id(2L).clientid("clientid2").accesstoken("accesstoken2");
    }

    public static Gitrep getGitrepRandomSampleGenerator() {
        return new Gitrep()
            .id(longCount.incrementAndGet())
            .clientid(UUID.randomUUID().toString())
            .accesstoken(UUID.randomUUID().toString());
    }
}
