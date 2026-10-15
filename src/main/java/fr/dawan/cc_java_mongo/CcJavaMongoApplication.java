package fr.dawan.cc_java_mongo;

import fr.dawan.fixture.DemoFixtureApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/**
 * Classe principale de l'application Spring Boot.
 * <p>
 * Cette application fournit une API REST de gestion médicale utilisant
 * Spring Boot 3.5.16, Java 21 et MongoDB. Elle gère les patients, médecins,
 * médicaments, consultations et documents associés, avec une authentification
 * basée sur JWT.
 * </p>
 *
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class CcJavaMongoApplication {

	/**
	 * Point d'entrée de l'application Spring Boot.
	 *
	 * @param args arguments de ligne de commande transmis à Spring Boot
	 */
	public static void main(String[] args) {
		if (isFixtureLaunch(args)) {
			new SpringApplicationBuilder(DemoFixtureApplication.class)
					.web(WebApplicationType.NONE)
					.run(args);
			return;
		}
		SpringApplication.run(CcJavaMongoApplication.class, args);
	}

	static boolean isFixtureLaunch(String[] args) {
		boolean fixtureArgument = Arrays.stream(args).anyMatch(argument -> {
			if (argument.startsWith("--app.demo-fixture.enabled=")) {
				return "true".equalsIgnoreCase(argument.substring(argument.indexOf('=') + 1));
			}
			if (argument.startsWith("--spring.profiles.active=")) {
				return hasFixtureProfile(argument.substring(argument.indexOf('=') + 1));
			}
			return false;
		});
		return fixtureArgument
				|| "true".equalsIgnoreCase(System.getProperty("app.demo-fixture.enabled"))
				|| hasFixtureProfile(System.getProperty("spring.profiles.active", ""))
				|| "true".equalsIgnoreCase(System.getenv("APP_DEMOFIXTURE_ENABLED"))
				|| hasFixtureProfile(System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", ""));
	}

	private static boolean hasFixtureProfile(String profiles) {
		return Arrays.stream(profiles.split(","))
				.map(String::trim)
				.anyMatch("demo-fixture"::equals);
	}

}
