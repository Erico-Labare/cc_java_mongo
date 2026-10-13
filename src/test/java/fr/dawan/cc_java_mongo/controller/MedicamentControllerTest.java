
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedicamentRequest;
import fr.dawan.cc_java_mongo.dto.MedicamentResponse;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.service.MedicamentService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicamentControllerTest {

    @Mock
    private MedicamentService medicamentService;

    private MedicamentController controller;

    @BeforeEach
    void setUp() {
        controller = new MedicamentController(medicamentService);
    }

    @Test
    void getMedicamentsShouldReturnMappedResponses() {
        when(medicamentService.getAllMedicaments())
                .thenReturn(List.of(
                        new Medicament("MEDIC001", "Paracetamol"),
                        new Medicament("MEDIC002", "Ibuprofene")));

        List<MedicamentResponse> result = controller.getMedicaments();

        assertEquals(2, result.size());
        assertEquals("MEDIC001", result.get(0).getCode());
        assertEquals("Paracetamol", result.get(0).getLibelle());
        assertEquals("MEDIC002", result.get(1).getCode());

        verify(medicamentService).getAllMedicaments();
    }

    @Test
    void getMedicamentsShouldReturnEmptyListWhenNoMedicamentExists() {
        when(medicamentService.getAllMedicaments()).thenReturn(List.of());

        List<MedicamentResponse> result = controller.getMedicaments();

        assertEquals(0, result.size());
    }

    @Test
    void getMedicamentShouldReturnMappedResponse() {
        when(medicamentService.getMedicamentById("MEDIC001"))
                .thenReturn(new Medicament("MEDIC001", "Paracetamol"));

        MedicamentResponse result = controller.getMedicament("MEDIC001");

        assertEquals("MEDIC001", result.getCode());
        assertEquals("Paracetamol", result.getLibelle());
        verify(medicamentService).getMedicamentById("MEDIC001");
    }

    @Test
    void createMedicamentShouldConvertRequestAndReturnResponse() {
        MedicamentRequest request = new MedicamentRequest();
        request.setCode("MEDIC003");
        request.setLibelle("Aspirine");

        when(medicamentService.createMedicament(any(Medicament.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicamentResponse result = controller.createMedicament(request);

        assertEquals("MEDIC003", result.getCode());
        assertEquals("Aspirine", result.getLibelle());
        verify(medicamentService).createMedicament(any(Medicament.class));
    }

    @Test
    void updateMedicamentShouldUsePathCodeAndRequestLibelle() {
        MedicamentRequest request = new MedicamentRequest();
        request.setCode("CODE_IGNORE");
        request.setLibelle("Nouveau libelle");

        when(medicamentService.updateMedicament(
                eq("MEDIC001"), any(Medicament.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        MedicamentResponse result =
                controller.updateMedicament("MEDIC001", request);

        assertEquals("MEDIC001", result.getCode());
        assertEquals("Nouveau libelle", result.getLibelle());

        verify(medicamentService).updateMedicament(
                eq("MEDIC001"), any(Medicament.class));
    }

    @Test
    void deleteMedicamentShouldDelegateToService() {
        controller.deleteMedicament("MEDIC001");

        verify(medicamentService).deleteMedicament("MEDIC001");
    }
}

