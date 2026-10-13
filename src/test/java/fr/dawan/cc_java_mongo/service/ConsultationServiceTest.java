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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.getConsultationsByPatient("PAT999", pageable)
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


    @Test
    void getAllConsultationsShouldReturnRepositoryPage() {
        var pageable = PageRequest.of(0, 10);
        var consultation = createValidConsultation();
        var expectedPage = new PageImpl<>(List.of(consultation), pageable, 1);

        when(consultationRepository.findAll(pageable)).thenReturn(expectedPage);

        var result = consultationService.getAllConsultations(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CONS001", result.getContent().get(0).getNumero());
        verify(consultationRepository).findAll(pageable);
    }

    @Test
    void getConsultationsByPatientShouldReturnRepositoryPage() {
        var pageable = PageRequest.of(0, 10);
        var consultation = createValidConsultation();
        var expectedPage = new PageImpl<>(List.of(consultation), pageable, 1);

        when(patientRepository.existsById("PAT001")).thenReturn(true);
        when(consultationRepository.findByPatientId("PAT001", pageable))
                .thenReturn(expectedPage);

        var result = consultationService.getConsultationsByPatient("PAT001", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CONS001", result.getContent().get(0).getNumero());
        verify(patientRepository).existsById("PAT001");
        verify(consultationRepository).findByPatientId("PAT001", pageable);
    }

    @Test
    void getConsultationByIdShouldReturnConsultationWhenFound() {
        var consultation = createValidConsultation();
        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));

        var result = consultationService.getConsultationById("CONS001");

        assertSame(consultation, result);
    }

    @Test
    void createConsultationShouldRejectBlankNumber() {
        var consultation = createValidConsultation();
        consultation.setNumero(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.createConsultation(consultation)
        );

        verifyNoInteractions(consultationRepository);
    }

    @Test
    void createConsultationShouldRejectDuplicateNumber() {
        var consultation = createValidConsultation();
        when(consultationRepository.existsById("CONS001")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.createConsultation(consultation)
        );

        verify(consultationRepository).existsById("CONS001");
        verify(consultationRepository, never()).save(any());
        verifyNoInteractions(patientRepository, medecinRepository, medicamentRepository);
    }

    @Test
    void updateConsultationShouldUpdateExistingConsultation() {
        var existing = createValidConsultation();
        var update = createValidConsultation();
        update.setDate(LocalDateTime.now().plusDays(5));
        update.setPatientId("PAT002");
        update.setMedecinId("MED002");

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(existing));
        when(patientRepository.existsById("PAT002")).thenReturn(true);
        when(medecinRepository.existsById("MED002")).thenReturn(true);
        when(medicamentRepository.existsById("MEDIC001")).thenReturn(true);
        when(consultationRepository.save(any(Consultation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = consultationService.updateConsultation("CONS001", update);

        assertEquals("CONS001", result.getNumero());
        assertEquals("PAT002", result.getPatientId());
        assertEquals("MED002", result.getMedecinId());
        assertEquals(update.getDate(), result.getDate());
        verify(consultationRepository).save(existing);
    }

    @Test
    void updateConsultationShouldReplaceNullPrescriptionsWithEmptyList() {
        var existing = createValidConsultation();
        var update = createValidConsultation();
        update.setPrescriptions(null);

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(existing));
        when(patientRepository.existsById("PAT001")).thenReturn(true);
        when(medecinRepository.existsById("MED001")).thenReturn(true);
        when(consultationRepository.save(any(Consultation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = consultationService.updateConsultation("CONS001", update);

        assertNotNull(result.getPrescriptions());
        assertTrue(result.getPrescriptions().isEmpty());
    }

    @Test
    void updateConsultationShouldRejectPastConsultation() {
        var existing = createValidConsultation();
        existing.setDate(LocalDateTime.now().minusDays(1));

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.updateConsultation(
                        "CONS001", createValidConsultation())
        );

        verify(consultationRepository, never()).save(any());
        verifyNoInteractions(patientRepository, medecinRepository, medicamentRepository);
    }

    @Test
    void deleteConsultationShouldDeleteFutureConsultation() {
        var consultation = createValidConsultation();
        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));

        consultationService.deleteConsultation("CONS001");

        verify(consultationRepository).delete(consultation);
    }

    @Test
    void deleteConsultationShouldRejectPastConsultation() {
        var consultation = createValidConsultation();
        consultation.setDate(LocalDateTime.now().minusDays(1));
        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.deleteConsultation("CONS001")
        );

        verify(consultationRepository, never()).delete(any());
    }

    @Test
    void updatePrescriptionsShouldSaveValidPrescriptions() {
        var consultation = createValidConsultation();
        var prescription = new Prescription();
        prescription.setMedicamentId("MEDIC002");
        prescription.setNbPrises(3);

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));
        when(medicamentRepository.existsById("MEDIC002")).thenReturn(true);
        when(consultationRepository.save(consultation)).thenReturn(consultation);

        var result = consultationService.updatePrescriptions(
                "CONS001", List.of(prescription));

        assertEquals(1, result.getPrescriptions().size());
        assertEquals("MEDIC002", result.getPrescriptions().get(0).getMedicamentId());
        assertEquals(3, result.getPrescriptions().get(0).getNbPrises());
        verify(consultationRepository).save(consultation);
    }

    @Test
    void updatePrescriptionsShouldReplaceNullWithEmptyList() {
        var consultation = createValidConsultation();
        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));
        when(consultationRepository.save(consultation)).thenReturn(consultation);

        var result = consultationService.updatePrescriptions("CONS001", null);

        assertNotNull(result.getPrescriptions());
        assertTrue(result.getPrescriptions().isEmpty());
        verify(consultationRepository).save(consultation);
    }

    @Test
    void updatePrescriptionsShouldRejectUnknownMedication() {
        var consultation = createValidConsultation();
        var prescription = new Prescription();
        prescription.setMedicamentId("MEDIC999");
        prescription.setNbPrises(1);

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));
        when(medicamentRepository.existsById("MEDIC999")).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> consultationService.updatePrescriptions(
                        "CONS001", List.of(prescription))
        );

        verify(consultationRepository, never()).save(any());
    }

    @Test
    void updatePrescriptionsShouldRejectZeroDoses() {
        var consultation = createValidConsultation();
        var prescription = new Prescription();
        prescription.setMedicamentId("MEDIC001");
        prescription.setNbPrises(0);

        when(consultationRepository.findById("CONS001"))
                .thenReturn(Optional.of(consultation));
        when(medicamentRepository.existsById("MEDIC001")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> consultationService.updatePrescriptions(
                        "CONS001", List.of(prescription))
        );

        verify(consultationRepository, never()).save(any());
    }
}
