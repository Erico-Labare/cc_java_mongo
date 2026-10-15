package fr.dawan.cc_java_mongo.security;

import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.security.bootstrap-user.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/cc_java_mongo_roles_test",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVm"
})
@AutoConfigureMockMvc
class RoleBasedSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private MedicamentRepository medicamentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PATIENT1_USERNAME = "patient1";
    private static final String PATIENT1_ID = "123456789012345";

    private static final String PATIENT2_USERNAME = "patient2";
    private static final String PATIENT2_ID = "987654321098765";

    private static final String UNLINKED_PATIENT_ID = "555555555555555";

    private static final String MEDECIN_USERNAME = "medecin1";
    private static final String MEDECIN_ID = "MED001";
    private static final String CONSULTATION_ID = "SECURITY-CONS-001";
    private static final String MEDICATION_ID = "SECURITY-MED-001";

    @BeforeEach
    void setUp() {
        // Les identifiants dédiés rendent les fixtures idempotentes sans effacer
        // les données éventuellement présentes dans la base Mongo locale.
        Patient patient1 = new Patient(PATIENT1_ID, "Patient1");
        Patient patient2 = new Patient(PATIENT2_ID, "Patient2");
        Patient unlinkedPatient = new Patient(UNLINKED_PATIENT_ID, "PatientNonRattache");
        patient1.setMedecinId(MEDECIN_ID);
        patient2.setMedecinId(MEDECIN_ID);
        patientRepository.save(patient1);
        patientRepository.save(patient2);
        patientRepository.save(unlinkedPatient);
        consultationRepository.save(new Consultation(CONSULTATION_ID,
                java.time.LocalDateTime.now().plusDays(10), PATIENT1_ID, MEDECIN_ID));
        medicamentRepository.save(new Medicament(MEDICATION_ID, "Test medication"));

        saveUser(PATIENT1_USERNAME, Role.PATIENT, PATIENT1_ID, null);
        saveUser(PATIENT2_USERNAME, Role.PATIENT, PATIENT2_ID, null);
        saveUser(MEDECIN_USERNAME, Role.MEDECIN, null, MEDECIN_ID);
    }

    private void saveUser(String username, Role role, String patientId, String medecinId) {
        utilisateurRepository.findByUsername(username).ifPresent(utilisateurRepository::delete);
        utilisateurRepository.save(new Utilisateur(
                null, username, passwordEncoder.encode("test-only-password"), role,
                patientId, medecinId));
    }

    @AfterEach
    void tearDown() {
        utilisateurRepository.findByUsername(PATIENT1_USERNAME).ifPresent(utilisateurRepository::delete);
        utilisateurRepository.findByUsername(PATIENT2_USERNAME).ifPresent(utilisateurRepository::delete);
        utilisateurRepository.findByUsername(MEDECIN_USERNAME).ifPresent(utilisateurRepository::delete);
        patientRepository.findById(PATIENT1_ID).ifPresent(patientRepository::delete);
        patientRepository.findById(PATIENT2_ID).ifPresent(patientRepository::delete);
        patientRepository.findById(UNLINKED_PATIENT_ID).ifPresent(patientRepository::delete);
        consultationRepository.findById(CONSULTATION_ID).ifPresent(consultationRepository::delete);
        medicamentRepository.findById(MEDICATION_ID).ifPresent(medicamentRepository::delete);
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCanAccessOwnPatientData() throws Exception {
        mockMvc.perform(get("/api/patients/" + PATIENT1_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSecuriteSociale").value(PATIENT1_ID));
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotAccessOtherPatientData() throws Exception {
        mockMvc.perform(get("/api/patients/" + PATIENT2_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCanOnlySeeOwnPatientsInList() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].numeroSecuriteSociale").value(PATIENT1_ID));
    }

    @Test
    @WithMockUser(username = MEDECIN_USERNAME, authorities = "ROLE_MEDECIN")
    void medecinCanAccessOnlyPatientsAssignedToHim() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser(username = MEDECIN_USERNAME, authorities = "ROLE_MEDECIN")
    void medecinCannotAccessUnlinkedPatient() throws Exception {
        mockMvc.perform(get("/api/patients/" + UNLINKED_PATIENT_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotAccessMedecinData() throws Exception {
        mockMvc.perform(get("/api/medecins"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = MEDECIN_USERNAME, authorities = "ROLE_MEDECIN")
    void medecinCanAccessMedecinData() throws Exception {
        mockMvc.perform(get("/api/medecins"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void unauthorizedAccessToAdminEndpointShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotModifyOtherPatientData() throws Exception {
        String updateJson = String.format("{\"numeroSecuriteSociale\":\"%s\",\"nom\":\"Modified\"}", PATIENT2_ID);
        
        mockMvc.perform(put("/api/patients/" + PATIENT2_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotDeleteOtherPatientData() throws Exception {
        mockMvc.perform(delete("/api/patients/" + PATIENT2_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotLearnWhetherAnotherPatientIdExists() throws Exception {
        mockMvc.perform(get("/api/patients/999999999999999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotMutateMedicationCatalog() throws Exception {
        mockMvc.perform(post("/api/medicaments").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"NEW\",\"libelle\":\"New\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/medicaments/" + MEDICATION_ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + MEDICATION_ID + "\",\"libelle\":\"Changed\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/medicaments/" + MEDICATION_ID)).andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertEquals("Test medication",
                medicamentRepository.findById(MEDICATION_ID).orElseThrow().getLibelle());
    }

    @Test
    @WithMockUser(username = PATIENT1_USERNAME, authorities = "ROLE_PATIENT")
    void patientCannotMutateOwnConsultationOrPrescriptions() throws Exception {
        String originalPatientId = consultationRepository.findById(CONSULTATION_ID).orElseThrow().getPatientId();
        mockMvc.perform(put("/api/consultations/" + CONSULTATION_ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":\"" + CONSULTATION_ID + "\",\"date\":\"2030-01-01T10:00:00\",\"patientId\":\"" + PATIENT1_ID + "\",\"medecinId\":\"" + MEDECIN_ID + "\",\"prescriptions\":[]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/consultations/" + CONSULTATION_ID)).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/consultations/" + CONSULTATION_ID + "/prescriptions")
                .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isForbidden());
        org.junit.jupiter.api.Assertions.assertEquals(originalPatientId,
                consultationRepository.findById(CONSULTATION_ID).orElseThrow().getPatientId());
    }

    @Test
    void publicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
