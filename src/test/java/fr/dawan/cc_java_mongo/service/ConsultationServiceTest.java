package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private MedicamentRepository medicamentRepository;

    private ConsultationService consultationService;

    @BeforeEach
    void setUp() {
        consultationService = new ConsultationService(
                consultationRepository,
                patientRepository,
                medecinRepository,
                medicamentRepository
        );
    }

    @Test
    void createConsultationShouldSaveValidConsultation() {
        Consultation consultation = createValidConsultation();

        when(consultationRepository.existsById("CONS001"))
                .thenReturn(false);
        when(patientRepository.existsById("PAT001"))
                .thenReturn(true);
        when(medecinRepository.existsById("MED001"))
                .thenReturn(true);
        when(medicamentRepository.existsById("MEDIC001"))
                .thenReturn(true);
        when(consultationRepository.save(consultation))
                .thenReturn(consultation);

        Consultation result =
                consultationService.createConsultation(consultation);

        assertEquals("CONS001", result.getNumero());
        assertEquals("PAT001", result.getPatientId());
        assertEquals("MED001", result.getMedecinId());

        verify(consultationRepository).existsById("CONS001");
        verify(patientRepository).existsById("PAT001");
        verify(medecinRepository).existsById("MED001");
        verify(medicamentRepository).existsById("MEDIC001");
        verify(consultationRepository).save(consultation);
    }

    @Test
    void createConsultationShouldRejectPastDate() {
        Consultation consultation = createValidConsultation();
        consultation.setDate(LocalDateTime.now().minusDays(1));

        when(consultationRepository.existsById("CONS001"))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.createConsultation(consultation)
        );

        verify(consultationRepository).existsById("CONS001");
        verifyNoInteractions(
                patientRepository,
                medecinRepository,
                medicamentRepository
        );
    }

    @Test
    void createConsultationShouldRejectUnknownPatient() {
        Consultation consultation = createValidConsultation();

        when(consultationRepository.existsById("CONS001"))
                .thenReturn(false);
        when(patientRepository.existsById("PAT001"))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.createConsultation(consultation)
        );

        verify(patientRepository).existsById("PAT001");
        verifyNoInteractions(medecinRepository, medicamentRepository);
    }

    @Test
    void createConsultationShouldRejectUnknownMedication() {
        Consultation consultation = createValidConsultation();

        when(consultationRepository.existsById("CONS001"))
                .thenReturn(false);
        when(patientRepository.existsById("PAT001"))
                .thenReturn(true);
        when(medecinRepository.existsById("MED001"))
                .thenReturn(true);
        when(medicamentRepository.existsById("MEDIC001"))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.createConsultation(consultation)
        );

        verify(medicamentRepository).existsById("MEDIC001");
    }

    @Test
    void createConsultationShouldRejectZeroDoses() {
        Consultation consultation = createValidConsultation();
        consultation.getPrescriptions().get(0).setNbPrises(0);

        when(consultationRepository.existsById("CONS001"))
                .thenReturn(false);
        when(patientRepository.existsById("PAT001"))
                .thenReturn(true);
        when(medecinRepository.existsById("MED001"))
                .thenReturn(true);
        when(medicamentRepository.existsById("MEDIC001"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.createConsultation(consultation)
        );
    }

    @Test
    void getConsultationByIdShouldThrowWhenNotFound() {
        when(consultationRepository.findById("INCONNUE"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.getConsultationById("INCONNUE")
        );

        verify(consultationRepository).findById("INCONNUE");
    }

    @Test
    void getConsultationsByPatientShouldRejectUnknownPatient() {
        when(patientRepository.existsById("PAT999"))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.getConsultationsByPatient(
                        "PAT999",
                        org.springframework.data.domain.PageRequest.of(0, 10)
                )
        );

        verify(patientRepository).existsById("PAT999");
    }

    private Consultation createValidConsultation() {
        Prescription prescription = new Prescription();
        prescription.setMedicamentId("MEDIC001");
        prescription.setNbPrises(2);

        Consultation consultation = new Consultation();
        consultation.setNumero("CONS001");
        consultation.setDate(LocalDateTime.now().plusDays(2));
        consultation.setPatientId("PAT001");
        consultation.setMedecinId("MED001");
        consultation.setPrescriptions(List.of(prescription));

        return consultation;
    }
}
