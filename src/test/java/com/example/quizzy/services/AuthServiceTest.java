package com.example.quizzy.services;

import com.example.quizzy.dto.RegisterUserDto;
import com.example.quizzy.entity.Role;
import com.example.quizzy.entity.User;
import com.example.quizzy.repository.RoleRepository;
import com.example.quizzy.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthService class.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register user successfully when username and email are not taken")
    void register_shouldSucceed_whenUsernameAndEmailAreUnique() {
        // Arrange
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("newUser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");

        Role userRole = new Role(1, "ROLE_USER");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        // Act & Assert
        assertDoesNotThrow(() -> authService.register(registerDto));

        // Verify that save method was called exactly once on the user repository
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when username is already taken")
    void register_shouldThrowException_whenUsernameIsTaken() {
        // Arrange
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("existingUser");
        registerDto.setEmail("newuser@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        // Act
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            authService.register(registerDto);
        });

        // Assert
        assertEquals("Username is already taken!", exception.getMessage());

        // Verify that save method was never called
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when email is already taken")
    void register_shouldThrowException_whenEmailIsTaken() {
        // Arrange
        RegisterUserDto registerDto = new RegisterUserDto();
        registerDto.setUsername("newUser");
        registerDto.setEmail("existing@example.com");
        registerDto.setPassword("password123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> authService.register(registerDto));
        verify(userRepository, never()).save(any());
    }
}
