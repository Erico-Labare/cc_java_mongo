package fr.dawan.cc_java_mongo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
		SpringApplication.run(CcJavaMongoApplication.class, args);
	}

}
