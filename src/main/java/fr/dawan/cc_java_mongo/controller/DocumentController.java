
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.service.DocumentService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Contrôleur REST pour la gestion des documents associés aux consultations.
 * <p>
 * Ce contrôleur expose les endpoints pour l'upload et le téléchargement
 * de documents stockés dans MongoDB GridFS :
 * </p>
 * <ul>
 * <li>{@code POST /api/consultations/{numero}/document} - Upload d'un document</li>
 * <li>{@code GET /api/consultations/{numero}/document} - Téléchargement d'un document</li>
 * </ul>
 * <p>
 * Les documents sont stockés dans GridFS et associés à une consultation
 * via son identifiant GridFS. Si un document existe déjà pour une consultation,
 * il est remplacé lors d'un nouvel upload.
 * Tous les endpoints nécessitent une authentification JWT valide.
 * </p>
 *
 * @see fr.dawan.cc_java_mongo.service.DocumentService
 * @see org.springframework.data.mongodb.gridfs.GridFsTemplate
 */
@RestController
@RequestMapping("/api/consultations")
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Construit le contrôleur avec le service des documents.
     *
     * @param documentService le service métier des documents
     */
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Ajoute ou remplace le document d'une consultation.
     */
    @PostMapping(
            value = "/{numero}/document",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadDocument(
            @PathVariable String numero,
            @RequestParam("file") MultipartFile file) throws IOException {

        Consultation consultation =
                documentService.uploadDocument(numero, file);

        return "Document enregistré. Identifiant : "
                + consultation.getDocumentId();
    }

    /**
     * Télécharge le document d'une consultation.
     */
    @GetMapping("/{numero}/document")
    public ResponseEntity<InputStreamResource> downloadDocument(
            @PathVariable String numero) throws IOException {

        GridFsResource resource =
                documentService.downloadDocument(numero);


        String filename = resource.getFilename();

        if (filename == null || filename.isBlank()) {
            filename = "document";
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (resource.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(
                        resource.getContentType());
            } catch (IllegalArgumentException ignored) {
                // On conserve le type générique si le type est invalide.
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename
                                .replace("\"", "") + "\"")
                .body(new InputStreamResource(resource.getInputStream()));
    }
}
