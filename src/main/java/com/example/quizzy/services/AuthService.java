package com.example.quizzy.services;

import com.example.quizzy.dto.RegisterUserDto;
import com.example.quizzy.entity.Role;
import com.example.quizzy.entity.User;
import com.example.quizzy.repository.RoleRepository;
import com.example.quizzy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service class for handling user authentication logic,
 * such as registration.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user in the system.
     * It validates if the username and email are unique, encodes the password,
     * assigns a default 'ROLE_USER', and saves the new user to the database.
     *
     * @param registerDto DTO containing user registration data.
     * @throws IllegalStateException if the username or email is already taken.
     */
    @Transactional
    public void register(RegisterUserDto registerDto) {
        // Check if the username is already taken
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new IllegalStateException("Username is already taken!");
        }

        // Check if the email is already in use
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email is already in use!");
        }

        // Create a new user entity
        User newUser = new User();
        newUser.setUsername(registerDto.getUsername());
        newUser.setEmail(registerDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign the default user role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_USER role not found."));
        newUser.setRoles(Set.of(userRole));

        // Save the new user to the database
        userRepository.save(newUser);
    }
}
