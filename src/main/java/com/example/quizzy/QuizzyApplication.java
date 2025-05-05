package com.example.quizzy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.quizzy.entity")
@EnableJpaRepositories("com.example.quizzy.repository")
public class QuizzyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizzyApplication.class, args);
	}

}
