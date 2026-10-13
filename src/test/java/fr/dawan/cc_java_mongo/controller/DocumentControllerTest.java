
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.model.Consultation;
import fr.dawan.cc_java_mongo.service.DocumentService;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private Consultation consultation;

    @Mock
    private GridFsResource gridFsResource;

    private DocumentController controller;

    @BeforeEach
    void setUp() {
        controller = new DocumentController(documentService);
    }

    @Test
    void uploadDocumentShouldReturnDocumentIdentifier() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "ordonnance.pdf",
                "application/pdf",
                "contenu".getBytes());

        when(documentService.uploadDocument("CONS001", file))
                .thenReturn(consultation);
        when(consultation.getDocumentId()).thenReturn("DOC001");

        String result = controller.uploadDocument("CONS001", file);

        assertEquals(
                "Document enregistré. Identifiant : DOC001", result);
        verify(documentService).uploadDocument("CONS001", file);
    }

    @Test
    void downloadDocumentShouldReturnFileAndContentType()
            throws IOException {
        when(documentService.downloadDocument("CONS001"))
                .thenReturn(gridFsResource);
        when(gridFsResource.getFilename()).thenReturn("ordonnance.pdf");
        when(gridFsResource.getContentType()).thenReturn("application/pdf");
        when(gridFsResource.getInputStream())
                .thenReturn(new ByteArrayInputStream("pdf".getBytes()));

        ResponseEntity<InputStreamResource> response =
                controller.downloadDocument("CONS001");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders()
                .getContentType());
        assertEquals(
                "attachment; filename=\"ordonnance.pdf\"",
                response.getHeaders().getFirst(
                        HttpHeaders.CONTENT_DISPOSITION));
        assertNotNull(response.getBody());

        verify(documentService).downloadDocument("CONS001");
    }

    @Test
    void downloadDocumentShouldUseDefaultFilenameWhenFilenameIsNull()
            throws IOException {
        when(documentService.downloadDocument("CONS001"))
                .thenReturn(gridFsResource);
        when(gridFsResource.getFilename()).thenReturn(null);
        when(gridFsResource.getContentType()).thenReturn(null);
        when(gridFsResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        ResponseEntity<InputStreamResource> response =
                controller.downloadDocument("CONS001");

        assertEquals(
                "attachment; filename=\"document\"",
                response.getHeaders().getFirst(
                        HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                response.getHeaders().getContentType());
    }

    @Test
    void downloadDocumentShouldUseDefaultFilenameWhenFilenameIsBlank()
            throws IOException {
        when(documentService.downloadDocument("CONS001"))
                .thenReturn(gridFsResource);
        when(gridFsResource.getFilename()).thenReturn("   ");
        when(gridFsResource.getContentType()).thenReturn(null);
        when(gridFsResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        ResponseEntity<InputStreamResource> response =
                controller.downloadDocument("CONS001");

        assertEquals(
                "attachment; filename=\"document\"",
                response.getHeaders().getFirst(
                        HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void downloadDocumentShouldFallbackWhenContentTypeIsInvalid()
            throws IOException {
        when(documentService.downloadDocument("CONS001"))
                .thenReturn(gridFsResource);
        when(gridFsResource.getFilename()).thenReturn("ordonnance.bin");
        when(gridFsResource.getContentType()).thenReturn("type-invalide");
        when(gridFsResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        ResponseEntity<InputStreamResource> response =
                controller.downloadDocument("CONS001");

        assertEquals(
                MediaType.APPLICATION_OCTET_STREAM,
                response.getHeaders().getContentType());
    }

    @Test
    void downloadDocumentShouldRemoveQuotesFromFilename()
            throws IOException {
        when(documentService.downloadDocument("CONS001"))
                .thenReturn(gridFsResource);
        when(gridFsResource.getFilename()).thenReturn("mon\"fichier.pdf");
        when(gridFsResource.getContentType()).thenReturn("application/pdf");
        when(gridFsResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(new byte[0]));

        ResponseEntity<InputStreamResource> response =
                controller.downloadDocument("CONS001");

        assertEquals(
                "attachment; filename=\"monfichier.pdf\"",
                response.getHeaders().getFirst(
                        HttpHeaders.CONTENT_DISPOSITION));
    }
}
