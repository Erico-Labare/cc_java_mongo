
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
 * Expose les endpoints REST de gestion des documents.
 */
@RestController
@RequestMapping("/api/consultations")
public class DocumentController {

    private final DocumentService documentService;

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

        String filename = resource.getFilename() == null
                ? "document"
                : resource.getFilename();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (resource.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(
                        resource.getContentType());
            } catch (IllegalArgumentException ignored) {
                // On conserve le type générique si le type est invalide.
            }
        }


        if (filename == null || filename.isBlank()) {
            filename = "document";
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
