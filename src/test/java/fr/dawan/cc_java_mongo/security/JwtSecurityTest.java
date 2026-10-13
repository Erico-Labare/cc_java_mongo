package fr.dawan.cc_java_mongo.security;

import fr.dawan.cc_java_mongo.dto.LoginRequest;
import fr.dawan.cc_java_mongo.model.Utilisateur;
import fr.dawan.cc_java_mongo.repository.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWYwMTIzNDU2Nzg5YWJjZGVm"
})
@AutoConfigureMockMvc
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "testuser_jwt";
    private static final String TEST_PASSWORD = "TestPassword123!";
    private static final String TEST_WRONG_PASSWORD = "WrongPassword123!";

    @BeforeEach
    void setUp() {
        utilisateurRepository.findByUsername(TEST_USERNAME)
                .ifPresent(utilisateurRepository::delete);

        Utilisateur utilisateur = new Utilisateur(
                null,
                TEST_USERNAME,
                passwordEncoder.encode(TEST_PASSWORD),
                "USER"
        );

        utilisateurRepository.save(utilisateur);
    }

    @AfterEach
    void tearDown() {
        utilisateurRepository.findByUsername(TEST_USERNAME)
                .ifPresent(utilisateurRepository::delete);
    }

    @Test
    void loginWithValidCredentialsShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(TEST_USERNAME);
        request.setPassword(TEST_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"" + TEST_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    void loginWithWrongPasswordShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"" + TEST_WRONG_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownUserShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown_user\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpointWithoutTokenShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpointWithInvalidTokenShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpointWithMalformedTokenShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessProtectedEndpointWithWrongPrefixShouldReturn403() throws Exception {
        String token = obtainValidToken();
        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Basic " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocsEndpointShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private String obtainValidToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + TEST_USERNAME
                                + "\",\"password\":\"" + TEST_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        return objectMapper.readTree(responseBody)
                .get("token")
                .asText();
    }

    @Test
    void accessProtectedEndpointWithValidTokenShouldSucceed()
            throws Exception {
        String token = obtainValidToken();

        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
