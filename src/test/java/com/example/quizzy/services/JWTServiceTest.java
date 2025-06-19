package com.example.quizzy.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    // Secret must be a valid key for HS256, i.e., at least 32 bytes long.
    private final String testSecret = "a_very_long_and_secure_secret_for_testing_purpose_only_32_bytes";
    private final int testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        // Manually inject values for @Value fields
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    @DisplayName("generateToken should create a valid JWT with correct claims")
    void generateToken_shouldCreateValidToken() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        doReturn(Set.of(new SimpleGrantedAuthority("ROLE_USER"))).when(authentication).getAuthorities();

        // Act
        String token = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(token);

        // Parse the token to verify its claims
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testuser", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("roles", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    @DisplayName("getUsernameFromToken should extract username from a valid token")
    void getUsernameFromToken_withValidToken_shouldReturnUsername() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        String token = jwtService.generateToken(authentication);

        // Act
        String extractedUsername = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", extractedUsername);
    }

    @Test
    @DisplayName("validateToken should return true for a valid token")
    void validateToken_withValidToken_shouldReturnTrue() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        String token = jwtService.generateToken(authentication);

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken should return false for an expired token")
    void validateToken_withExpiredToken_shouldReturnFalse() {
        // Arrange
        // Temporarily set expiration to a negative value to generate an expired token
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        String expiredToken = jwtService.generateToken(authentication);

        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", testExpirationMs);

        // Act
        boolean isValid = jwtService.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken should return false for a token with an invalid signature")
    void validateToken_withInvalidSignature_shouldReturnFalse() throws InterruptedException {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(Collections.emptySet());
        String token = jwtService.generateToken(authentication);

        // Create another service with a different secret to validate the token
        JwtService anotherJwtService = new JwtService();
        ReflectionTestUtils.setField(anotherJwtService, "jwtSecret", "a_completely_different_secret_key_that_is_also_long_enough");

        // Act
        boolean isValid = anotherJwtService.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken should return false for a malformed or invalid token string")
    void validateToken_withMalformedToken_shouldReturnFalse() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt";

        // Act
        boolean isValid = jwtService.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken should return false for an empty or null token")
    void validateToken_withEmptyToken_shouldReturnFalse() {
        assertFalse(jwtService.validateToken(null));
        assertFalse(jwtService.validateToken(""));
    }
}