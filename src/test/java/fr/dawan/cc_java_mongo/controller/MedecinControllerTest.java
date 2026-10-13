
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.MedecinRequest;
import fr.dawan.cc_java_mongo.dto.MedecinResponse;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.service.MedecinService;

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
class MedecinControllerTest {

    @Mock
    private MedecinService medecinService;

    private MedecinController controller;

    @BeforeEach
    void setUp() {
        controller = new MedecinController(medecinService);
    }

    @Test
    void getMedecinsWithoutNameShouldReturnAllMedecins() {
        var pageable = PageRequest.of(0, 10);
        var medecin = new Medecin("MED001", "Martin");

        when(medecinService.getAllMedecins(pageable))
                .thenReturn(new PageImpl<>(List.of(medecin)));

        Page<MedecinResponse> result =
                controller.getMedecins(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("MED001", result.getContent().get(0).getMatricule());
        assertEquals("Martin", result.getContent().get(0).getNom());

        verify(medecinService).getAllMedecins(pageable);
    }

    @Test
    void getMedecinsWithBlankNameShouldReturnAllMedecins() {
        var pageable = PageRequest.of(0, 10);

        when(medecinService.getAllMedecins(pageable))
                .thenReturn(Page.empty(pageable));

        Page<MedecinResponse> result =
                controller.getMedecins("   ", pageable);

        assertEquals(0, result.getTotalElements());
        verify(medecinService).getAllMedecins(pageable);
    }

    @Test
    void getMedecinsWithNameShouldSearchMedecins() {
        var pageable = PageRequest.of(0, 10);
        var medecin = new Medecin("MED002", "Durand");

        when(medecinService.searchMedecins("Durand", pageable))
                .thenReturn(new PageImpl<>(List.of(medecin)));

        Page<MedecinResponse> result =
                controller.getMedecins("Durand", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("MED002", result.getContent().get(0).getMatricule());
        assertEquals("Durand", result.getContent().get(0).getNom());

        verify(medecinService).searchMedecins("Durand", pageable);
    }

    @Test
    void getMedecinShouldReturnMedecinResponse() {
        when(medecinService.getMedecinById("MED001"))
                .thenReturn(new Medecin("MED001", "Martin"));

        MedecinResponse result = controller.getMedecin("MED001");

        assertEquals("MED001", result.getMatricule());
        assertEquals("Martin", result.getNom());
        verify(medecinService).getMedecinById("MED001");
    }

    @Test
    void createMedecinShouldConvertRequestAndReturnResponse() {
        MedecinRequest request = new MedecinRequest();
        request.setMatricule("MED003");
        request.setNom("Bernard");

        when(medecinService.createMedecin(any(Medecin.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedecinResponse result = controller.createMedecin(request);

        assertEquals("MED003", result.getMatricule());
        assertEquals("Bernard", result.getNom());

        verify(medecinService).createMedecin(any(Medecin.class));
    }

    @Test
    void updateMedecinShouldUsePathMatriculeAndRequestName() {
        MedecinRequest request = new MedecinRequest();
        request.setMatricule("IGNORED");
        request.setNom("Nouveau nom");

        when(medecinService.updateMedecin(eq("MED001"), any(Medecin.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        MedecinResponse result =
                controller.updateMedecin("MED001", request);

        assertEquals("MED001", result.getMatricule());
        assertEquals("Nouveau nom", result.getNom());

        verify(medecinService)
                .updateMedecin(eq("MED001"), any(Medecin.class));
    }

    @Test
    void deleteMedecinShouldDelegateToService() {
        controller.deleteMedecin("MED001");

        verify(medecinService).deleteMedecin("MED001");
    }
}
