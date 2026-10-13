
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.PatientRequest;
import fr.dawan.cc_java_mongo.dto.PatientResponse;
import fr.dawan.cc_java_mongo.model.Patient;
import fr.dawan.cc_java_mongo.service.PatientService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    private PatientController controller;

    @BeforeEach
    void setUp() {
        controller = new PatientController(patientService);
    }

    @Test
    void getPatientsWithoutNameShouldReturnAllPatients() {
        var pageable = PageRequest.of(0, 10);
        when(patientService.getAllPatients(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(new Patient("PAT001", "Martin"))));

        Page<PatientResponse> result = controller.getPatients(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PAT001", result.getContent().get(0)
                .getNumeroSecuriteSociale());
        assertEquals("Martin", result.getContent().get(0).getNom());

        verify(patientService).getAllPatients(pageable);
    }

    @Test
    void getPatientsWithBlankNameShouldReturnAllPatients() {
        var pageable = PageRequest.of(0, 10);
        when(patientService.getAllPatients(pageable))
                .thenReturn(Page.empty(pageable));

        Page<PatientResponse> result =
                controller.getPatients("  ", pageable);

        assertEquals(0, result.getTotalElements());
        verify(patientService).getAllPatients(pageable);
    }

    @Test
    void getPatientsWithNameShouldSearchPatients() {
        var pageable = PageRequest.of(0, 10);
        when(patientService.searchPatients("Durand", pageable))
                .thenReturn(new PageImpl<>(
                        List.of(new Patient("PAT002", "Durand"))));

        Page<PatientResponse> result =
                controller.getPatients("Durand", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("PAT002", result.getContent().get(0)
                .getNumeroSecuriteSociale());

        verify(patientService).searchPatients("Durand", pageable);
    }

    @Test
    void getPatientShouldReturnMappedResponse() {
        when(patientService.getPatientById("PAT001"))
                .thenReturn(new Patient("PAT001", "Martin"));

        PatientResponse result = controller.getPatient("PAT001");

        assertEquals("PAT001", result.getNumeroSecuriteSociale());
        assertEquals("Martin", result.getNom());
        verify(patientService).getPatientById("PAT001");
    }

    @Test
    void createPatientShouldConvertRequestAndReturnResponse() {
        PatientRequest request = new PatientRequest();
        request.setNumeroSecuriteSociale("PAT003");
        request.setNom("Bernard");

        when(patientService.createPatient(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientResponse result = controller.createPatient(request);

        assertEquals("PAT003", result.getNumeroSecuriteSociale());
        assertEquals("Bernard", result.getNom());

        verify(patientService).createPatient(any(Patient.class));
    }

    @Test
    void updatePatientShouldUsePathIdentifier() {
        PatientRequest request = new PatientRequest();
        request.setNumeroSecuriteSociale("IDENTIFIANT_IGNORE");
        request.setNom("Nouveau nom");

        when(patientService.updatePatient(
                eq("PAT001"), any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        PatientResponse result =
                controller.updatePatient("PAT001", request);

        assertEquals("PAT001", result.getNumeroSecuriteSociale());
        assertEquals("Nouveau nom", result.getNom());

        verify(patientService).updatePatient(
                eq("PAT001"), any(Patient.class));
    }

    @Test
    void deletePatientShouldDelegateToService() {
        controller.deletePatient("PAT001");

        verify(patientService).deletePatient("PAT001");
    }
}

