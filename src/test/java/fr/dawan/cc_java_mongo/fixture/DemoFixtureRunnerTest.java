package fr.dawan.cc_java_mongo.fixture;

import com.mongodb.client.MongoDatabase;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.model.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DemoFixtureRunnerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-10-15T12:00:00Z"), ZoneId.of("Europe/Paris"));

    @Test
    void rejectsAnyDatabaseOtherThanMedicaldb() {
        assertThrows(IllegalStateException.class,
                () -> DemoFixtureRunner.requireAuthorizedDatabase("otherdb"));
        assertThrows(IllegalStateException.class,
                () -> DemoFixtureRunner.requireAuthorizedDatabase(""));
        assertThrows(IllegalStateException.class,
                () -> DemoFixtureRunner.requireAuthorizedDatabase(null));
    }

    @Test
    void rejectsTestDatabasesIncludingMedicaldbTest() {
        assertThrows(IllegalStateException.class,
                () -> DemoFixtureRunner.requireAuthorizedDatabase("medicaldb_test"));
        assertThrows(IllegalStateException.class,
                () -> DemoFixtureRunner.requireAuthorizedDatabase("cc_java_mongo_roles_test"));
    }

    @Test
    void resetRejectsWrongResolvedDatabaseBeforeAnyWrite() {
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner runner = runner(factory("another_database"), template,
                "RESET_DEMO_DATA\n");

        assertThrows(IllegalStateException.class, runner::reset);
        verifyNoInteractions(template);
    }

    @Test
    void incorrectConfirmationDoesNotDeleteAnything() {
        MongoDatabaseFactory factory = factory("medicaldb");
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner runner = runner(factory, template, "yes\n");

        assertThrows(IllegalStateException.class, runner::reset);
        verifyNoInteractions(template);
    }

    @Test
    void unavailableDirectConsoleInputStopsBeforeAnyWrite() {
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner runner = runner(factory("medicaldb"), template, "");
        String oldConfirmation = System.getProperty("app.demo-fixture.confirmation");
        try {
            System.clearProperty("app.demo-fixture.confirmation");
            assertThrows(IllegalStateException.class, runner::reset);
            verifyNoInteractions(template);
        } finally {
            if (oldConfirmation != null) {
                System.setProperty("app.demo-fixture.confirmation", oldConfirmation);
            }
        }
    }

    @Test
    void suppliedConfirmationMustStillMatchExactlyBeforeAnyWrite() {
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner runner = runner(factory("medicaldb"), template, "");
        String oldConfirmation = System.getProperty("app.demo-fixture.confirmation");
        try {
            System.setProperty("app.demo-fixture.confirmation", "RESET_DEMO_DATA ");
            assertThrows(IllegalStateException.class, runner::reset);
            verifyNoInteractions(template);
        } finally {
            if (oldConfirmation == null) {
                System.clearProperty("app.demo-fixture.confirmation");
            } else {
                System.setProperty("app.demo-fixture.confirmation", oldConfirmation);
            }
        }
    }

    @Test
    void exactSuppliedConfirmationAllowsScriptLaunchWithoutSecondConsoleInput() {
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner runner = runner(factory("medicaldb"), template, "");
        String oldConfirmation = System.getProperty("app.demo-fixture.confirmation");
        try {
            System.setProperty("app.demo-fixture.confirmation", "RESET_DEMO_DATA");
            runner.reset();
            verify(template, times(DemoFixtureRunner.COLLECTIONS.size()))
                    .remove(any(Query.class), anyString());
        } finally {
            if (oldConfirmation == null) {
                System.clearProperty("app.demo-fixture.confirmation");
            } else {
                System.setProperty("app.demo-fixture.confirmation", oldConfirmation);
            }
        }
    }

    @Test
    void insertionFailureIsReportedAndNeverPrintedAsSuccess() {
        MongoTemplate template = mock(MongoTemplate.class);
        doThrow(new IllegalStateException("simulated insert failure"))
                .when(template).insert(anyList(), eq(Medecin.class));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        DemoFixtureRunner runner = new DemoFixtureRunner(factory("medicaldb"), template,
                new BCryptPasswordEncoder(), new Scanner("RESET_DEMO_DATA\n"),
                new PrintStream(captured));

        IllegalStateException failure = assertThrows(IllegalStateException.class, runner::run);

        assertTrue(failure.getMessage().contains("n'a pas pu être terminé"));
        assertTrue(captured.toString(StandardCharsets.UTF_8).contains("ÉCHEC du fixture"));
        assertNull(failure.getCause(), "La cause détaillée est journalisée séparément et expurgée.");
        assertFalse(captured.toString(StandardCharsets.UTF_8)
                .contains("Fixture de démonstration installé"));
        verify(template, times(DemoFixtureRunner.COLLECTIONS.size()))
                .remove(any(Query.class), anyString());
    }

    @Test
    void failureStackTraceIsUsefulButRedactsMongoCredentials() {
        RuntimeException failure = new RuntimeException(
                "connection failed at mongodb://demo-user:s3cr3t@db.example/medicaldb");

        String safeStackTrace = DemoFixtureRunner.sanitizedStackTrace(failure);

        assertTrue(safeStackTrace.contains("RuntimeException: connection failed"));
        assertTrue(safeStackTrace.contains("mongodb://[IDENTIFIANTS MASQUÉS]@db.example/medicaldb"));
        assertFalse(safeStackTrace.contains("demo-user"));
        assertFalse(safeStackTrace.contains("s3cr3t"));
    }

    @Test
    void onlyExplicitlyEnabledAndInvokedRunnerCanReset() {
        MongoTemplate template = mock(MongoTemplate.class);
        new ApplicationContextRunner()
                .withUserConfiguration(FixtureConfiguration.class)
                .withBean(MongoTemplate.class, () -> template)
                .run(context -> {
                    assertTrue(context.getBeansOfType(DemoFixtureRunner.class).isEmpty());
                    verifyNoFixtureWrites(template);
                });
    }

    @Test
    void explicitEnablePropertyRegistersRunnerWithoutRunningItDuringContextRefresh() {
        MongoTemplate template = mock(MongoTemplate.class);
        new ApplicationContextRunner()
                .withUserConfiguration(FixtureConfiguration.class)
                .withBean(MongoTemplate.class, () -> template)
                .withPropertyValues("app.demo-fixture.enabled=true")
                .run(context -> {
                    assertEquals(1, context.getBeansOfType(DemoFixtureRunner.class).size());
                    verifyNoFixtureWrites(template);
                });
    }

    @Test
    void runnerRequiresTheExplicitEnableProperty() {
        ConditionalOnProperty condition = DemoFixtureRunner.class
                .getAnnotation(ConditionalOnProperty.class);
        assertNotNull(condition);
        assertArrayEquals(new String[]{"app.demo-fixture.enabled"}, condition.name());
        assertEquals("true", condition.havingValue());
    }

    @Test
    void confirmedResetUsesOnlyAllowedCollectionsAndIsRepeatable() {
        MongoTemplate template = mock(MongoTemplate.class);
        DemoFixtureRunner first = runner(factory("medicaldb"), template,
                "RESET_DEMO_DATA\n");
        DemoFixtureRunner second = runner(factory("medicaldb"), template,
                "RESET_DEMO_DATA\n");

        first.reset();
        second.reset();

        for (String collection : DemoFixtureRunner.COLLECTIONS) {
            verify(template, times(2)).remove(any(Query.class), eq(collection));
        }
        verify(template, never()).remove(any(Query.class), eq("fs.files"));
        verify(template, never()).remove(any(Query.class), eq("fs.chunks"));
        verify(template, times(2)).insert(anyList(), eq(Medecin.class));
        verify(template, times(2)).insert(anyList(), eq(Patient.class));
        verify(template, times(2)).insert(anyList(), eq(Medicament.class));
        verify(template, times(2)).insert(anyList(), eq(Consultation.class));
        verify(template, times(2)).insert(anyList(), eq(Utilisateur.class));
    }

    @Test
    void repeatedRunsProduceStableDemoIdsAndConsultationDates() {
        MongoTemplate template = mock(MongoTemplate.class);
        runner(factory("medicaldb"), template, "RESET_DEMO_DATA\n").reset();
        runner(factory("medicaldb"), template, "RESET_DEMO_DATA\n").reset();

        @SuppressWarnings("unchecked")
        var consultations = org.mockito.ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        var accounts = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(template, times(2)).insert(consultations.capture(), eq(Consultation.class));
        verify(template, times(2)).insert(accounts.capture(), eq(Utilisateur.class));
        List<Consultation> firstConsultations = consultations.getAllValues().get(0);
        List<Consultation> secondConsultations = consultations.getAllValues().get(1);
        assertEquals(firstConsultations.stream().map(Consultation::getNumero).toList(),
                secondConsultations.stream().map(Consultation::getNumero).toList());
        assertEquals(firstConsultations.stream().map(Consultation::getDate).toList(),
                secondConsultations.stream().map(Consultation::getDate).toList());
        List<Utilisateur> firstAccounts = accounts.getAllValues().get(0);
        List<Utilisateur> secondAccounts = accounts.getAllValues().get(1);
        assertEquals(firstAccounts.stream().map(Utilisateur::getId).toList(),
                secondAccounts.stream().map(Utilisateur::getId).toList());
        assertEquals(firstAccounts.stream().map(Utilisateur::getRole).toList(),
                secondAccounts.stream().map(Utilisateur::getRole).toList());
    }

    @Test
    void fixtureReferencesAreCoherentAndPasswordsAreEncoded() {
        MongoTemplate template = mock(MongoTemplate.class);
        runner(factory("medicaldb"), template, "RESET_DEMO_DATA\n").reset();

        @SuppressWarnings("unchecked")
        var doctorCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        var patientCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        var medicationCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        var consultationCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        var accountCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(template).insert(doctorCaptor.capture(), eq(Medecin.class));
        verify(template).insert(patientCaptor.capture(), eq(Patient.class));
        verify(template).insert(medicationCaptor.capture(), eq(Medicament.class));
        verify(template).insert(consultationCaptor.capture(), eq(Consultation.class));
        verify(template).insert(accountCaptor.capture(), eq(Utilisateur.class));

        List<Medecin> doctors = doctorCaptor.getValue();
        List<Patient> patients = patientCaptor.getValue();
        List<Medicament> medications = medicationCaptor.getValue();
        List<Consultation> consultations = consultationCaptor.getValue();
        List<Utilisateur> accounts = accountCaptor.getValue();
        assertEquals(3, doctors.size());
        assertEquals(6, patients.size());
        assertEquals(8, medications.size());
        assertEquals(6, consultations.size());
        assertEquals(10, accounts.size());
        assertEquals(List.of("Dr Claire Moreau", "Dr Thomas Bernard", "Dr Nadia Petit"),
                doctors.stream().map(Medecin::getNom).toList());
        assertEquals(List.of("Emma Lefèvre", "Lucas Dubois", "Inès Garnier", "Hugo Fontaine",
                        "Manon Chevalier", "Antoine Leroy"),
                patients.stream().map(Patient::getNom).toList());
        assertTrue(patients.stream().allMatch(patient ->
                patient.getNumeroSecuriteSociale().matches("DEMO-PAT-[0-9]{3}")));
        assertEquals(List.of("Paracétamol", "Ibuprofène", "Amoxicilline", "Cétirizine",
                        "Oméprazole", "Salbutamol", "Metformine", "Atorvastatine"),
                medications.stream().map(Medicament::getLibelle).toList());

        var doctorIds = doctors.stream().map(Medecin::getMatricule).collect(java.util.stream.Collectors.toSet());
        var patientIds = patients.stream().map(Patient::getNumeroSecuriteSociale).collect(java.util.stream.Collectors.toSet());
        var medicationIds = medications.stream().map(Medicament::getCode).collect(java.util.stream.Collectors.toSet());
        var prescribedMedicationIds = new java.util.HashSet<String>();
        patients.forEach(patient -> assertTrue(doctorIds.contains(patient.getMedecinId())));
        consultations.forEach(consultation -> {
            assertTrue(patientIds.contains(consultation.getPatientId()));
            assertTrue(doctorIds.contains(consultation.getMedecinId()));
            Patient linkedPatient = patients.stream()
                    .filter(patient -> patient.getNumeroSecuriteSociale().equals(consultation.getPatientId()))
                    .findFirst().orElseThrow();
            assertEquals(linkedPatient.getMedecinId(), consultation.getMedecinId());
            assertTrue(consultation.getDate().isAfter(java.time.LocalDateTime.now(FIXED_CLOCK)));
            assertNotEquals(java.time.DayOfWeek.SATURDAY, consultation.getDate().getDayOfWeek());
            assertNotEquals(java.time.DayOfWeek.SUNDAY, consultation.getDate().getDayOfWeek());
            consultation.getPrescriptions().forEach(prescription -> {
                assertTrue(medicationIds.contains(prescription.getMedicamentId()));
                prescribedMedicationIds.add(prescription.getMedicamentId());
                assertTrue(prescription.getNbPrises() > 0);
            });
        });
        assertEquals(medicationIds, prescribedMedicationIds);
        assertEquals(java.util.Set.of("admin.demo", "claire.moreau", "thomas.bernard", "nadia.petit",
                        "emma.lefevre", "lucas.dubois", "ines.garnier", "hugo.fontaine",
                        "manon.chevalier", "antoine.leroy"),
                accounts.stream().map(Utilisateur::getUsername).collect(java.util.stream.Collectors.toSet()));
        assertTrue(accounts.stream().filter(user -> user.getRole() == fr.dawan.cc_java_mongo.model.Role.PATIENT)
                .allMatch(user -> patientIds.contains(user.getPatientId())));
        assertTrue(accounts.stream().filter(user -> user.getRole() == fr.dawan.cc_java_mongo.model.Role.MEDECIN)
                .allMatch(user -> doctorIds.contains(user.getMedecinId())));
        assertTrue(accounts.stream().allMatch(user -> new BCryptPasswordEncoder().matches(
                "DemoOnly-2026!", user.getPassword())));
    }

    private DemoFixtureRunner runner(MongoDatabaseFactory factory, MongoTemplate template, String answer) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        return new DemoFixtureRunner(factory, template, new BCryptPasswordEncoder(),
                new Scanner(answer), new PrintStream(captured), FIXED_CLOCK);
    }

    private void verifyNoFixtureWrites(MongoTemplate template) {
        verify(template, never()).remove(any(Query.class), anyString());
        verify(template, never()).insert(anyList(), any(Class.class));
    }

    private MongoDatabaseFactory factory(String name) {
        MongoDatabase database = mock(MongoDatabase.class);
        when(database.getName()).thenReturn(name);
        MongoDatabaseFactory factory = mock(MongoDatabaseFactory.class);
        when(factory.getMongoDatabase()).thenReturn(database);
        return factory;
    }

    @Configuration(proxyBeanMethods = false)
    @Import(DemoFixtureRunner.class)
    static class FixtureConfiguration {
        @Bean MongoDatabaseFactory mongoDatabaseFactory() { return mock(MongoDatabaseFactory.class); }
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }
}
