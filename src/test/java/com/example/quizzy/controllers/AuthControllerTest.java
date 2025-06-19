package com.example.quizzy.controllers;

import com.example.quizzy.dto.LoginRequestDto;
import com.example.quizzy.dto.RegisterUserDto;
import com.example.quizzy.services.AuthService;
import com.example.quizzy.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the AuthController class.
 * These tests verify the behavior of the registration and login endpoints.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    /**
     * A helper ControllerAdvice to handle exceptions for the standalone MockMvc setup.
     * This simulates the global exception handling mechanism of a Spring application.
     */
    @ControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    /**
     * Sets up the MockMvc instance before each test.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new TestGlobalExceptionHandler())
                .build();
    }

    /**
     * Test case for the POST /api/auth/register endpoint.
     * Verifies that a user can be successfully registered.
     */
    @Test
    void registerUser_whenValidDetailsProvided_shouldReturnCreated() throws Exception {
        // Arrange
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername("testuser");
        registerUserDto.setEmail("test@example.com");
        registerUserDto.setPassword("password123");

        doNothing().when(authService).register(any(RegisterUserDto.class));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully!"));

        verify(authService, times(1)).register(any(RegisterUserDto.class));
    }

    /**
     * Test case for the POST /api/auth/login endpoint.
     * Verifies that a user can successfully authenticate and receive a JWT.
     */
    @Test
    void authenticateUser_whenCredentialsAreValid_shouldReturnJwtToken() throws Exception {
        // Arrange
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername("testuser");
        loginRequestDto.setPassword("password");

        String expectedJwt = "mock.jwt.token";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateToken(authentication)).thenReturn(expectedJwt);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedJwt))
                .andExpect(jsonPath("$.type").value("Bearer"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(authentication);
    }

    /**
     * Test case for the POST /api/auth/login endpoint with invalid credentials.
     * Verifies that the authentication fails with a 401 Unauthorized status.
     */
    @Test
    void authenticateUser_whenCredentialsAreInvalid_shouldReturnUnauthorized() throws Exception {
        // Arrange
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setUsername("wronguser");
        loginRequestDto.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));

        verify(jwtService, never()).generateToken(any(Authentication.class));
    }
}