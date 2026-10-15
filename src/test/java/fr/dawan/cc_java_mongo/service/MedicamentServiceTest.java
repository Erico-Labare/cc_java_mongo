package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Medicament;
import fr.dawan.cc_java_mongo.repository.MedicamentRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class MedicamentServiceTest {

    @Mock
    private MedicamentRepository medicamentRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private MockedStatic<SecurityUtils> securityUtils;

    private MedicamentService medicamentService;

    @BeforeEach
    void setUp() {
        securityUtils = mockStatic(SecurityUtils.class);
        securityUtils.when(() -> SecurityUtils.getCurrentRole(utilisateurRepository)).thenReturn(Role.ADMIN);
        medicamentService = new MedicamentService(medicamentRepository, utilisateurRepository);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() { securityUtils.close(); }

    @Test
    void patientCannotMutateMedicationCatalogThroughService() {
        securityUtils.when(() -> SecurityUtils.getCurrentRole(utilisateurRepository)).thenReturn(Role.PATIENT);
        assertThrows(fr.dawan.cc_java_mongo.exception.AccessDeniedException.class,
                () -> medicamentService.createMedicament(new Medicament("NEW", "New")));
        assertThrows(fr.dawan.cc_java_mongo.exception.AccessDeniedException.class,
                () -> medicamentService.updateMedicament("OLD", new Medicament("OLD", "Changed")));
        assertThrows(fr.dawan.cc_java_mongo.exception.AccessDeniedException.class,
                () -> medicamentService.deleteMedicament("OLD"));
        verifyNoInteractions(medicamentRepository);
    }

    @Test
    void getAllMedicamentsShouldReturnList() {
        Medicament medicament = new Medicament();
        medicament.setCode("MED001");
        medicament.setLibelle("Paracetamol");

        when(medicamentRepository.findAll())
                .thenReturn(List.of(medicament));

        List<Medicament> result = medicamentService.getAllMedicaments();

        assertEquals(1, result.size());
        assertEquals("MED001", result.get(0).getCode());
        assertEquals("Paracetamol", result.get(0).getLibelle());

        verify(medicamentRepository).findAll();
        verifyNoMoreInteractions(medicamentRepository);
    }

    @Test
    void getMedicamentByIdShouldReturnMedicamentWhenFound() {
        String code = "MED001";
        Medicament medicament = new Medicament();
        medicament.setCode(code);
        medicament.setLibelle("Paracetamol");

        when(medicamentRepository.findById(code))
                .thenReturn(Optional.of(medicament));

        Medicament result = medicamentService.getMedicamentById(code);

        assertEquals(code, result.getCode());
        assertEquals("Paracetamol", result.getLibelle());

        verify(medicamentRepository).findById(code);
        verifyNoMoreInteractions(medicamentRepository);
    }

    @Test
    void getMedicamentByIdShouldThrowWhenNotFound() {
        String code = "INCONNU";

        when(medicamentRepository.findById(code))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> medicamentService.getMedicamentById(code)
        );

        verify(medicamentRepository).findById(code);
        verifyNoMoreInteractions(medicamentRepository);
    }

    @Test
    void createMedicamentShouldSaveWhenCodeIsValidAndUnused() {
        String code = "MED002";
        Medicament medicament = new Medicament();
        medicament.setCode(code);
        medicament.setLibelle("Ibuprofene");

        when(medicamentRepository.existsById(code)).thenReturn(false);
        when(medicamentRepository.save(medicament)).thenReturn(medicament);

        Medicament result = medicamentService.createMedicament(medicament);

        assertEquals(code, result.getCode());
        assertEquals("Ibuprofene", result.getLibelle());

        verify(medicamentRepository).existsById(code);
        verify(medicamentRepository).save(medicament);
        verifyNoMoreInteractions(medicamentRepository);
    }

    @Test
    void createMedicamentShouldRejectDuplicateCode() {
        String code = "MED001";
        Medicament medicament = new Medicament();
        medicament.setCode(code);
        medicament.setLibelle("Paracetamol");

        when(medicamentRepository.existsById(code)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> medicamentService.createMedicament(medicament)
        );

        verify(medicamentRepository).existsById(code);
        verifyNoMoreInteractions(medicamentRepository);
    }

    @Test
    void createMedicamentShouldRejectBlankCode() {
        Medicament medicament = new Medicament();
        medicament.setCode(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> medicamentService.createMedicament(medicament)
        );

        verifyNoInteractions(medicamentRepository);
    }
}
