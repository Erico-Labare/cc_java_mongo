
package fr.dawan.cc_java_mongo.controller;

import fr.dawan.cc_java_mongo.dto.LoginRequest;
import fr.dawan.cc_java_mongo.dto.LoginResponse;
import fr.dawan.cc_java_mongo.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authenticationManager, jwtService);
    }

    @Test
    void loginShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("test-password");

        when(authenticationManager.authenticate(
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-test-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginResponse response = controller.login(request);

        assertEquals("jwt-test-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());

        verify(authenticationManager).authenticate(
                org.mockito.ArgumentMatchers.any());
        verify(jwtService).generateToken(userDetails);
        verify(jwtService).getExpirationMs();
    }
}

