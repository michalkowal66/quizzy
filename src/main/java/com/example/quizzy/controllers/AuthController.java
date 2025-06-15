package com.example.quizzy.controllers;

import com.example.quizzy.dto.JwtResponseDto;
import com.example.quizzy.dto.LoginRequestDto;
import com.example.quizzy.dto.RegisterUserDto;
import com.example.quizzy.services.AuthService;
import com.example.quizzy.services.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Endpoint for registering a new user.
     *
     * @param registerUserDto DTO containing new user's data (username, email, password).
     * @return A 201 Created status with a success message.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        authService.register(registerUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }

    /**
     * Endpoint for authenticating a user and generating a JWT.
     *
     * @param loginRequestDto DTO containing login credentials (username and password).
     * @return A 200 OK status with the JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        // Authenticate the user using Spring Security's standard mechanism
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsername(),
                        loginRequestDto.getPassword()
                )
        );

        // Set the authenticated user in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate the JWT
        String jwt = jwtService.generateToken(authentication);

        // Return the token in the response
        return ResponseEntity.ok(new JwtResponseDto(jwt));
    }
}