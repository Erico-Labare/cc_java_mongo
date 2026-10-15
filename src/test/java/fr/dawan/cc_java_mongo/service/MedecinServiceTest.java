package fr.dawan.cc_java_mongo.service;

import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Medecin;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.repository.MedecinRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedecinServiceTest {

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private MedecinService medecinService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = org.mockito.Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(() -> SecurityUtils.getCurrentRole(utilisateurRepository))
                .thenReturn(Role.ADMIN);
        medecinService = new MedecinService(medecinRepository, utilisateurRepository);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void getMedecinByIdShouldReturnMedecinWhenFound() {
        String matricule = "MED001";
        Medecin medecin = new Medecin();
        medecin.setMatricule(matricule);
        medecin.setNom("Durand");

        when(medecinRepository.findById(matricule))
                .thenReturn(Optional.of(medecin));

        Medecin result = medecinService.getMedecinById(matricule);

        assertEquals(matricule, result.getMatricule());
        assertEquals("Durand", result.getNom());

        verify(medecinRepository).findById(matricule);
        verifyNoMoreInteractions(medecinRepository);
    }

    @Test
    void getMedecinByIdShouldThrowWhenNotFound() {
        String matricule = "MED999";

        when(medecinRepository.findById(matricule))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> medecinService.getMedecinById(matricule)
        );

        verify(medecinRepository).findById(matricule);
        verifyNoMoreInteractions(medecinRepository);
    }

    @Test
    void createMedecinShouldSaveWhenMatriculeIsValidAndUnused() {
        String matricule = "MED002";
        Medecin medecin = new Medecin();
        medecin.setMatricule(matricule);
        medecin.setNom("Martin");

        when(medecinRepository.existsById(matricule))
                .thenReturn(false);
        when(medecinRepository.save(medecin))
                .thenReturn(medecin);

        Medecin result = medecinService.createMedecin(medecin);

        assertEquals(matricule, result.getMatricule());
        assertEquals("Martin", result.getNom());

        verify(medecinRepository).existsById(matricule);
        verify(medecinRepository).save(medecin);
        verifyNoMoreInteractions(medecinRepository);
    }

    @Test
    void createMedecinShouldRejectDuplicateMatricule() {
        String matricule = "MED001";
        Medecin medecin = new Medecin();
        medecin.setMatricule(matricule);
        medecin.setNom("Durand");

        when(medecinRepository.existsById(matricule))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> medecinService.createMedecin(medecin)
        );

        verify(medecinRepository).existsById(matricule);
        verifyNoMoreInteractions(medecinRepository);
    }

    @Test
    void createMedecinShouldRejectBlankMatricule() {
        Medecin medecin = new Medecin();
        medecin.setMatricule(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> medecinService.createMedecin(medecin)
        );

        verifyNoInteractions(medecinRepository);
    }
}
