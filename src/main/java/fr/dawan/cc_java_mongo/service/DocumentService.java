
package fr.dawan.cc_java_mongo.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import fr.dawan.cc_java_mongo.exception.ResourceNotFoundException;
import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.repository.ConsultationRepository;

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
 * Gère les documents associés aux consultations avec MongoDB GridFS.
 */
@Service
public class DocumentService {

    private final GridFsTemplate gridFsTemplate;
    private final ConsultationRepository consultationRepository;

    public DocumentService(
            GridFsTemplate gridFsTemplate,
            ConsultationRepository consultationRepository) {
        this.gridFsTemplate = gridFsTemplate;
        this.consultationRepository = consultationRepository;
    }

    /**
     * Ajoute un fichier à une consultation existante.
     */
    public Consultation uploadDocument(
            String numero, MultipartFile file) throws IOException {

        Consultation consultation = consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));

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
     */
    public GridFsResource downloadDocument(String numero) {
        Consultation consultation = consultationRepository.findById(numero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation introuvable : " + numero));

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

