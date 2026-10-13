
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.ConsultationRequest;
import fr.dawan.cc_java_mongo.dto.ConsultationResponse;
import fr.dawan.cc_java_mongo.dto.PrescriptionRequest;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Prescription;
import fr.dawan.cc_java_mongo.service.ConsultationService;

import java.time.LocalDateTime;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationControllerTest {

    @Mock
    private ConsultationService consultationService;

    private ConsultationController controller;

    @BeforeEach
    void setUp() {
        controller = new ConsultationController(consultationService);
    }

    private Consultation createConsultation() {
        Consultation consultation = new Consultation(
                "CONS001",
                LocalDateTime.now().plusDays(3),
                "PAT001",
                "MED001");

        consultation.setPrescriptions(List.of(
                new Prescription("MEDIC001", 2)));

        return consultation;
    }

    private ConsultationRequest createRequest() {
        ConsultationRequest request = new ConsultationRequest();
        request.setNumero("CONS001");
        request.setDate(LocalDateTime.now().plusDays(4));
        request.setPatientId("PAT001");
        request.setMedecinId("MED001");

        PrescriptionRequest prescription = new PrescriptionRequest();
        prescription.setMedicamentId("MEDIC001");
        prescription.setNbPrises(2);
        request.setPrescriptions(List.of(prescription));

        return request;
    }

    @Test
    void getConsultationsShouldReturnMappedPage() {
        var pageable = PageRequest.of(0, 10);
        when(consultationService.getAllConsultations(pageable))
                .thenReturn(new PageImpl<>(List.of(createConsultation())));

        Page<ConsultationResponse> result =
                controller.getConsultations(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CONS001", result.getContent().get(0).getNumero());
        assertEquals("PAT001", result.getContent().get(0).getPatientId());
        assertEquals(1, result.getContent().get(0).getPrescriptions().size());
        assertEquals("MEDIC001",
                result.getContent().get(0).getPrescriptions().get(0)
                        .getMedicamentId());

        verify(consultationService).getAllConsultations(pageable);
    }

    @Test
    void getConsultationsByPatientShouldReturnMappedPage() {
        var pageable = PageRequest.of(0, 10);
        when(consultationService.getConsultationsByPatient("PAT001", pageable))
                .thenReturn(new PageImpl<>(List.of(createConsultation())));

        Page<ConsultationResponse> result =
                controller.getConsultationsByPatient("PAT001", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("CONS001", result.getContent().get(0).getNumero());

        verify(consultationService)
                .getConsultationsByPatient("PAT001", pageable);
    }

    @Test
    void getConsultationShouldReturnMappedResponse() {
        when(consultationService.getConsultationById("CONS001"))
                .thenReturn(createConsultation());

        ConsultationResponse result =
                controller.getConsultation("CONS001");

        assertEquals("CONS001", result.getNumero());
        assertEquals("MED001", result.getMedecinId());
        assertNotNull(result.getDate());

        verify(consultationService).getConsultationById("CONS001");
    }

    @Test
    void createConsultationShouldConvertRequestAndReturnResponse() {
        when(consultationService.createConsultation(any(Consultation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationResponse result =
                controller.createConsultation(createRequest());

        assertEquals("CONS001", result.getNumero());
        assertEquals("PAT001", result.getPatientId());
        assertEquals("MED001", result.getMedecinId());
        assertEquals(1, result.getPrescriptions().size());

        verify(consultationService).createConsultation(any(Consultation.class));
    }

    @Test
    void updateConsultationShouldUsePathNumber() {
        when(consultationService.updateConsultation(
                eq("CONS001"), any(Consultation.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        ConsultationRequest request = createRequest();
        request.setNumero("AUTRE_NUMERO");

        ConsultationResponse result =
                controller.updateConsultation("CONS001", request);

        assertEquals("AUTRE_NUMERO", result.getNumero());

        verify(consultationService)
                .updateConsultation(eq("CONS001"), any(Consultation.class));
    }

    @Test
    void deleteConsultationShouldDelegateToService() {
        controller.deleteConsultation("CONS001");

        verify(consultationService).deleteConsultation("CONS001");
    }

    @Test
    void updatePrescriptionsShouldConvertRequests() {
        List<PrescriptionRequest> requests = List.of(
                prescriptionRequest("MEDIC002", 3));

        when(consultationService.updatePrescriptions(
                eq("CONS001"), any()))
                .thenReturn(createConsultation());

        ConsultationResponse result =
                controller.updatePrescriptions("CONS001", requests);

        assertEquals("CONS001", result.getNumero());

        verify(consultationService).updatePrescriptions(
                eq("CONS001"), any());
    }

    @Test
    void updatePrescriptionsShouldAcceptNullList() {
        when(consultationService.updatePrescriptions(eq("CONS001"), any()))
                .thenReturn(createConsultation());

        controller.updatePrescriptions("CONS001", null);

        verify(consultationService).updatePrescriptions(
                eq("CONS001"), any());
    }

    private PrescriptionRequest prescriptionRequest(
            String medicamentId, int nbPrises) {
        PrescriptionRequest request = new PrescriptionRequest();
        request.setMedicamentId(medicamentId);
        request.setNbPrises(nbPrises);
        return request;
    }
}

