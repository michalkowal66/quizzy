package com.example.quizzy.config;

import com.example.quizzy.entity.Role;
import com.example.quizzy.entity.User;
import com.example.quizzy.repository.RoleRepository;
import com.example.quizzy.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * This component runs on application startup to initialize necessary data,
 * such as default roles and a default admin user.
 */
@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Inject values from application.properties
    @Value("${admin.default.username}")
    private String adminUsername;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;


    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting data initialization...");
        initializeRoles();
        createDefaultAdmin();
        log.info("Data initialization finished.");
    }

    /**
     * Creates default roles (ROLE_USER, ROLE_ADMIN) if they don't exist.
     */
    private void initializeRoles() {
        initializeRole("ROLE_USER");
        initializeRole("ROLE_ADMIN");
    }

    private void initializeRole(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        }
    }

    /**
     * Creates a default administrator account if it does not already exist.
     * Credentials are read from application.properties.
     */
    private void createDefaultAdmin() {
        // Check if the admin user already exists
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_ADMIN not found."));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: ROLE_USER not found."));

            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRoles(Set.of(userRole, adminRole)); // Admin has both roles

            userRepository.save(adminUser);
            log.info("Default admin user '{}' created successfully.", adminUsername);
        } else {
            log.info("Default admin user '{}' already exists. Skipping creation.", adminUsername);
        }
    }
}
