
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Tag(name = "Documents", description = "Dépôt et téléchargement des documents associés aux consultations.")
@SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Déposer un document", description = "Enregistre un fichier multipart dans MongoDB GridFS et l'associe à une consultation. Le champ de formulaire est file. Un document existant est remplacé. Les patients ne peuvent pas déposer ; un médecin doit être celui de la consultation ; un administrateur peut déposer.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Document enregistré ; le corps texte contient son identifiant."), @ApiResponse(responseCode = "400", description = "Fichier vide, nom absent ou requête multipart invalide."), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Dépôt non autorisé pour ce rôle ou cette consultation."), @ApiResponse(responseCode = "404", description = "Consultation inexistante."), @ApiResponse(responseCode = "500", description = "Erreur lors du stockage du document.")})
    public String uploadDocument(
            @Parameter(description = "Numéro de la consultation cible.", example = "CONS-2026-001") @PathVariable String numero,
            @Parameter(description = "Fichier à déposer. Aucun format ni plafond de taille n'est imposé par le contrôleur.") @RequestParam("file") MultipartFile file) throws IOException {

        Consultation consultation =
                documentService.uploadDocument(numero, file);

        return "Document enregistré. Identifiant : "
                + consultation.getDocumentId();
    }

    /**
     * Télécharge le document d'une consultation.
     */
    @GetMapping("/{numero}/document")
    @Operation(summary = "Télécharger le document", description = "Renvoie le fichier associé avec son type de contenu enregistré, ou application/octet-stream si celui-ci est absent ou invalide. Patients : uniquement leurs consultations ; médecins : uniquement leurs consultations ; administrateurs : tous les documents.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Flux du document téléchargé.", content = @Content(schema = @Schema(type = "string", format = "binary"))), @ApiResponse(responseCode = "401", description = "Jeton absent ou invalide."), @ApiResponse(responseCode = "403", description = "Accès au document non autorisé."), @ApiResponse(responseCode = "404", description = "Consultation ou document absent.")})
    public ResponseEntity<InputStreamResource> downloadDocument(
            @Parameter(description = "Numéro de la consultation.", example = "CONS-2026-001") @PathVariable String numero) throws IOException {

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
