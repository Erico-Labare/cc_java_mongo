package fr.dawan.cc_java_mongo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CcJavaMongoApplicationLaunchTest {

    @Test
    void normalStartupUsesTheNormalApplicationContext() {
        assertFalse(CcJavaMongoApplication.isFixtureLaunch(new String[0]));
        assertFalse(CcJavaMongoApplication.isFixtureLaunch(
                new String[]{"--spring.profiles.active=local"}));
    }

    @Test
    void explicitFixturePropertyOrProfileSelectsTheMinimalContext() {
        assertTrue(CcJavaMongoApplication.isFixtureLaunch(
                new String[]{"--app.demo-fixture.enabled=true"}));
        assertTrue(CcJavaMongoApplication.isFixtureLaunch(
                new String[]{"--spring.profiles.active=local,demo-fixture"}));
    }
}
