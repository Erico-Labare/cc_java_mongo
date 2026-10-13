package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }

    @Test
    void getPatientByIdShouldReturnPatientWhenFound() {
        String numero = "123456789012345";
        Patient patient = new Patient();
        patient.setNumeroSecuriteSociale(numero);
        patient.setNom("Dupont");

        when(patientRepository.findById(numero))
                .thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(numero);

        assertEquals(numero, result.getNumeroSecuriteSociale());
        assertEquals("Dupont", result.getNom());

        verify(patientRepository).findById(numero);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void getPatientByIdShouldThrowWhenNotFound() {
        String numero = "123456789012345";

        when(patientRepository.findById(numero))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.getPatientById(numero)
        );

        verify(patientRepository).findById(numero);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void createPatientShouldSaveWhenIdIsValidAndUnused() {
        String numero = "123456789012345";
        Patient patient = new Patient();
        patient.setNumeroSecuriteSociale(numero);
        patient.setNom("Martin");

        when(patientRepository.existsById(numero)).thenReturn(false);
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = patientService.createPatient(patient);

        assertEquals(numero, result.getNumeroSecuriteSociale());
        assertEquals("Martin", result.getNom());

        verify(patientRepository).existsById(numero);
        verify(patientRepository).save(patient);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void createPatientShouldRejectDuplicateId() {
        String numero = "123456789012345";
        Patient patient = new Patient();
        patient.setNumeroSecuriteSociale(numero);
        patient.setNom("Martin");

        when(patientRepository.existsById(numero)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(patient)
        );

        verify(patientRepository).existsById(numero);
        verifyNoMoreInteractions(patientRepository);
    }

    @Test
    void createPatientShouldRejectBlankId() {
        Patient patient = new Patient();
        patient.setNumeroSecuriteSociale(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(patient)
        );

        verifyNoInteractions(patientRepository);
    }
}
