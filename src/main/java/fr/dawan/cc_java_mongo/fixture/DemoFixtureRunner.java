package fr.dawan.cc_java_mongo.fixture;

import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.model.Utilisateur;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/** Explicitly invoked destructive demo-data reset. */
@Component
@ConditionalOnProperty(name = "app.demo-fixture.enabled", havingValue = "true")
public class DemoFixtureRunner implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoFixtureRunner.class);
    private static final Pattern MONGO_URI_CREDENTIALS = Pattern.compile(
            "(?i)(mongodb(?:\\+srv)?://)[^\\s/]+@"
    );

    public static final String EXPECTED_DATABASE = "medicaldb";
    public static final String CONFIRMATION = "RESET_DEMO_DATA";
    public static final List<String> COLLECTIONS = List.of(
            "patients", "medecins", "medicaments", "consultations", "utilisateurs");

    private final MongoDatabaseFactory databaseFactory;
    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Scanner input;
    private final PrintStream output;
    private final Clock clock;

    @Autowired
    public DemoFixtureRunner(
            MongoDatabaseFactory databaseFactory,
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder) {
        this(databaseFactory, mongoTemplate, passwordEncoder, new Scanner(System.in), System.out,
                Clock.systemDefaultZone());
    }

    DemoFixtureRunner(
            MongoDatabaseFactory databaseFactory,
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder,
            Scanner input,
            PrintStream output) {
        this(databaseFactory, mongoTemplate, passwordEncoder, input, output, Clock.systemDefaultZone());
    }

    DemoFixtureRunner(
            MongoDatabaseFactory databaseFactory,
            MongoTemplate mongoTemplate,
            PasswordEncoder passwordEncoder,
            Scanner input,
            PrintStream output,
            Clock clock) {
        this.databaseFactory = databaseFactory;
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
        this.input = input;
        this.output = output;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        reset();
    }

    void reset() {
        String databaseName = databaseFactory.getMongoDatabase().getName();
        requireAuthorizedDatabase(databaseName);

        output.println("Base MongoDB cible : " + databaseName);
        output.println("Collections qui seront vidées : " + String.join(", ", COLLECTIONS));
        output.println("Les fichiers GridFS (fs.files et fs.chunks) seront préservés.");
        String suppliedConfirmation = System.getProperty("app.demo-fixture.confirmation");
        boolean confirmed;
        if (suppliedConfirmation != null) {
            confirmed = CONFIRMATION.equals(suppliedConfirmation);
        } else {
            output.print("Saisissez exactement " + CONFIRMATION + " pour continuer : ");
            output.flush();
            confirmed = input.hasNextLine() && CONFIRMATION.equals(input.nextLine());
        }
        if (!confirmed) {
            throw new IllegalStateException("Confirmation absente ou incorrecte; aucune donnée supprimée.");
        }

        try {
            COLLECTIONS.forEach(collection -> mongoTemplate.remove(
                    new org.springframework.data.mongodb.core.query.Query(), collection));
            insertFixture();
        } catch (RuntimeException failure) {
            output.println("ÉCHEC du fixture : la base peut être partiellement vidée ou remplie. "
                    + "Aucune transaction ne garantit l'annulation des écritures précédentes.");
            String safeStackTrace = sanitizedStackTrace(failure);
            LOGGER.error("Échec du remplissage du fixture. Cause et stack trace (URI MongoDB expurgées) :\n{}",
                    safeStackTrace);
            throw new IllegalStateException("Le fixture de démonstration n'a pas pu être terminé; "
                    + "vérifiez l'état des collections et les détails journalisés.");
        }
        output.println("Fixture de démonstration installé dans " + databaseName + ".");
    }

    static void requireAuthorizedDatabase(String databaseName) {
        if (databaseName == null || databaseName.isBlank()
                || databaseName.toLowerCase().contains("test")
                || !EXPECTED_DATABASE.equals(databaseName)) {
            throw new IllegalStateException(
                    "Réinitialisation refusée : la base résolue doit être exactement "
                            + EXPECTED_DATABASE + " et ne peut pas être une base de test.");
        }
    }

    static String sanitizedStackTrace(Throwable failure) {
        StringWriter buffer = new StringWriter();
        failure.printStackTrace(new PrintWriter(buffer));
        return MONGO_URI_CREDENTIALS.matcher(buffer.toString())
                .replaceAll("$1[IDENTIFIANTS MASQUÉS]@");
    }

    private void insertFixture() {
        Medecin claireMoreau = new Medecin("MED-DEMO-CMOREAU", "Dr Claire Moreau");
        Medecin thomasBernard = new Medecin("MED-DEMO-TBERNARD", "Dr Thomas Bernard");
        Medecin nadiaPetit = new Medecin("MED-DEMO-NPETIT", "Dr Nadia Petit");
        List<Medecin> doctors = List.of(claireMoreau, thomasBernard, nadiaPetit);

        // These IDs are synthetic fixture keys; they are not French social-security numbers.
        Patient emmaLefevre = patient("DEMO-PAT-001", "Emma Lefèvre", claireMoreau.getMatricule());
        Patient lucasDubois = patient("DEMO-PAT-002", "Lucas Dubois", claireMoreau.getMatricule());
        Patient inesGarnier = patient("DEMO-PAT-003", "Inès Garnier", thomasBernard.getMatricule());
        Patient hugoFontaine = patient("DEMO-PAT-004", "Hugo Fontaine", thomasBernard.getMatricule());
        Patient manonChevalier = patient("DEMO-PAT-005", "Manon Chevalier", nadiaPetit.getMatricule());
        Patient antoineLeroy = patient("DEMO-PAT-006", "Antoine Leroy", nadiaPetit.getMatricule());
        List<Patient> patients = List.of(
                emmaLefevre, lucasDubois, inesGarnier, hugoFontaine, manonChevalier, antoineLeroy);

        Medicament paracetamol = new Medicament("MED-DEMO-PARA", "Paracétamol");
        Medicament ibuprofene = new Medicament("MED-DEMO-IBUP", "Ibuprofène");
        Medicament amoxicilline = new Medicament("MED-DEMO-AMOX", "Amoxicilline");
        Medicament cetirizine = new Medicament("MED-DEMO-CETI", "Cétirizine");
        Medicament omeprazole = new Medicament("MED-DEMO-OMEP", "Oméprazole");
        Medicament salbutamol = new Medicament("MED-DEMO-SALB", "Salbutamol");
        Medicament metformine = new Medicament("MED-DEMO-METF", "Metformine");
        Medicament atorvastatine = new Medicament("MED-DEMO-ATOR", "Atorvastatine");
        List<Medicament> medications = List.of(
                paracetamol, ibuprofene, amoxicilline, cetirizine,
                omeprazole, salbutamol, metformine, atorvastatine);

        List<LocalDateTime> dates = demoConsultationDates();
        List<Consultation> consultations = List.of(
                consultation("CONS-DEMO-001", dates.get(0), emmaLefevre, claireMoreau,
                        new Prescription(paracetamol.getCode(), 2),
                        new Prescription(ibuprofene.getCode(), 1)),
                consultation("CONS-DEMO-002", dates.get(1), lucasDubois, claireMoreau,
                        new Prescription(cetirizine.getCode(), 1)),
                consultation("CONS-DEMO-003", dates.get(2), inesGarnier, thomasBernard,
                        new Prescription(omeprazole.getCode(), 1)),
                consultation("CONS-DEMO-004", dates.get(3), hugoFontaine, thomasBernard,
                        new Prescription(amoxicilline.getCode(), 3)),
                consultation("CONS-DEMO-005", dates.get(4), manonChevalier, nadiaPetit,
                        new Prescription(salbutamol.getCode(), 1)),
                consultation("CONS-DEMO-006", dates.get(5), antoineLeroy, nadiaPetit,
                        new Prescription(metformine.getCode(), 2),
                        new Prescription(atorvastatine.getCode(), 1)));

        mongoTemplate.insert(doctors, Medecin.class);
        mongoTemplate.insert(patients, Patient.class);
        mongoTemplate.insert(medications, Medicament.class);
        mongoTemplate.insert(consultations, Consultation.class);

        List<Utilisateur> accounts = new ArrayList<>();
        accounts.add(user("admin.demo", Role.ADMIN, null, null));
        accounts.add(user("claire.moreau", Role.MEDECIN, null, claireMoreau.getMatricule()));
        accounts.add(user("thomas.bernard", Role.MEDECIN, null, thomasBernard.getMatricule()));
        accounts.add(user("nadia.petit", Role.MEDECIN, null, nadiaPetit.getMatricule()));
        accounts.add(user("emma.lefevre", Role.PATIENT, emmaLefevre.getNumeroSecuriteSociale(), null));
        accounts.add(user("lucas.dubois", Role.PATIENT, lucasDubois.getNumeroSecuriteSociale(), null));
        accounts.add(user("ines.garnier", Role.PATIENT, inesGarnier.getNumeroSecuriteSociale(), null));
        accounts.add(user("hugo.fontaine", Role.PATIENT, hugoFontaine.getNumeroSecuriteSociale(), null));
        accounts.add(user("manon.chevalier", Role.PATIENT, manonChevalier.getNumeroSecuriteSociale(), null));
        accounts.add(user("antoine.leroy", Role.PATIENT, antoineLeroy.getNumeroSecuriteSociale(), null));
        mongoTemplate.insert(accounts, Utilisateur.class);
    }

    private List<LocalDateTime> demoConsultationDates() {
        LocalDateTime nextAppointment = LocalDateTime.of(Year.now(clock).getValue() + 1, 2, 1, 9, 30);
        while (nextAppointment.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                || nextAppointment.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            nextAppointment = nextAppointment.plusDays(1);
        }
        List<LocalDateTime> dates = new ArrayList<>();
        for (int week = 0; week < 6; week++) {
            dates.add(nextAppointment.plusWeeks(week));
        }
        return dates;
    }

    private Patient patient(String id, String name, String doctorId) {
        Patient patient = new Patient(id, name);
        patient.setMedecinId(doctorId);
        return patient;
    }

    private Consultation consultation(
            String id, LocalDateTime date, Patient patient, Medecin doctor, Prescription... prescriptions) {
        Consultation consultation = new Consultation(id, date,
                patient.getNumeroSecuriteSociale(), doctor.getMatricule());
        consultation.setPrescriptions(List.of(prescriptions));
        return consultation;
    }

    private Utilisateur user(String username, Role role, String patientId, String doctorId) {
        Utilisateur user = new Utilisateur();
        user.setId(username);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("DemoOnly-2026!"));
        user.setRole(role);
        user.setPatientId(patientId);
        user.setMedecinId(doctorId);
        return user;
    }
}
