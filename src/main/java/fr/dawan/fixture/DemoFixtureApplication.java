package fr.dawan.fixture;

import fr.dawan.cc_java_mongo.config.PasswordConfig;
import fr.dawan.cc_java_mongo.fixture.DemoFixtureRunner;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/** Minimal Spring context for the explicitly requested MongoDB demonstration fixture. */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
@EnableMongoRepositories(basePackageClasses = PatientRepository.class)
@Import({PasswordConfig.class, DemoFixtureRunner.class})
public class DemoFixtureApplication {
}
