
package fr.dawan.cc_java_mongo.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.exception.AccessDeniedException;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.model.Role;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import fr.dawan.cc_java_mongo.security.SecurityUtils;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Service métier pour la gestion des documents avec MongoDB GridFS.
 * <p>
 * Ce service gère le stockage et la récupération de fichiers associés
 * aux consultations médicales en utilisant MongoDB GridFS pour les fichiers
 * de taille importante. Il remplace automatiquement un document existant
 * si un nouveau est uploadé pour la même consultation.
 * </p>
 * <p>
 * <strong>Contrôles d'accès :</strong>
 * <ul>
 * <li>PATIENT : ne peut télécharger que les documents de ses consultations</li>
 * <li>MEDECIN : peut télécharger tous les documents</li>
 * <li>ADMIN : peut télécharger tous les documents</li>
 * </ul>
 *
 * @see org.springframework.data.mongodb.gridfs.GridFsTemplate
 * @see fr.dawan.cc_java_mongo.model.Consultation
 */
@Service
public class DocumentService {

    private final GridFsTemplate gridFsTemplate;
    private final ConsultationRepository consultationRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Construit le service avec le template GridFS et le repository des consultations.
     *
     * @param gridFsTemplate le template pour les opérations GridFS
     * @param consultationRepository le repository des consultations
     * @param utilisateurRepository le repository des utilisateurs
     */
    public DocumentService(
            GridFsTemplate gridFsTemplate,
            ConsultationRepository consultationRepository,
            UtilisateurRepository utilisateurRepository) {
        this.gridFsTemplate = gridFsTemplate;
        this.consultationRepository = consultationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Ajoute un fichier à une consultation existante.
     */
    public Consultation uploadDocument(
            String numero, MultipartFile file) throws IOException {

        Consultation consultation = consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));

        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);
        if (role == Role.PATIENT) {
            throw new AccessDeniedException("Un patient ne peut pas déposer un document de consultation.");
        }
        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null || !medecinId.equals(consultation.getMedecinId())) {
                throw new AccessDeniedException("Dépôt de document non autorisé pour cette consultation.");
            }
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Le fichier ne doit pas être vide.");
        }


        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du fichier est obligatoire.");
        }


        // Si un document existe déjà, on le remplace.
        if (consultation.getDocumentId() != null) {
            gridFsTemplate.delete(Query.query(
                    where("_id").is(
                            new ObjectId(consultation.getDocumentId()))));
        }

        ObjectId documentId;

        try (InputStream inputStream = file.getInputStream()) {
            documentId = gridFsTemplate.store(
                    inputStream,
                    file.getOriginalFilename(),
                    file.getContentType());
        }

        consultation.setDocumentId(documentId.toHexString());

        return consultationRepository.save(consultation);
    }

    /**
     * Récupère le document associé à une consultation.
     * <p>
     * L'accès est contrôlé selon le rôle de l'utilisateur :
     * <ul>
     * <li>PATIENT : ne peut télécharger que les documents de ses consultations</li>
     * <li>MEDECIN : peut télécharger tous les documents</li>
     * <li>ADMIN : peut télécharger tous les documents</li>
     * </ul>
     *
     * @param numero le numéro de la consultation
     * @return la ressource GridFS contenant le document
     * @throws ResourceNotFoundException si la consultation ou le document n'existe pas
     * @throws IllegalStateException si l'utilisateur n'a pas les droits d'accès
     */
    public GridFsResource downloadDocument(String numero) {
        Consultation consultation = consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));

        // Vérification des droits d'accès
        Role role = SecurityUtils.getCurrentRole(utilisateurRepository);

        if (role == Role.PATIENT) {
            String patientId = SecurityUtils.getCurrentPatientId(utilisateurRepository);
            if (patientId == null || !patientId.equals(consultation.getPatientId())) {
                throw new AccessDeniedException(
                        "Un patient ne peut accéder qu'aux documents de ses propres consultations.");
            }
        }

        if (role == Role.MEDECIN) {
            String medecinId = SecurityUtils.getCurrentMedecinId(utilisateurRepository);
            if (medecinId == null || !medecinId.equals(consultation.getMedecinId())) {
                throw new AccessDeniedException("Téléchargement non autorisé pour cette consultation.");
            }
        }

        if (consultation.getDocumentId() == null) {
            throw new ResourceNotFoundException(
                    "Aucun document associé à cette consultation.");
        }

        GridFSFile file = gridFsTemplate.findOne(
                Query.query(Criteria.where("_id")
                        .is(new ObjectId(consultation.getDocumentId()))));

        if (file == null) {
            throw new ResourceNotFoundException(
                    "Le document est introuvable dans GridFS.");
        }

        return gridFsTemplate.getResource(file);
    }
}

