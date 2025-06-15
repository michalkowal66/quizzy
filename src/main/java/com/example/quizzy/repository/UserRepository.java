package com.example.quizzy.repository;

import com.example.quizzy.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by their username.
     * @param username the username to search for.
     * @return an Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * This is useful for checking if an email is already registered.
     * @param email the email to search for.
     * @return an Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findByEmail(String email);
}