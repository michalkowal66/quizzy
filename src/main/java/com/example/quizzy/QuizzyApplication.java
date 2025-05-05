package com.example.quizzy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.quizzy.entity")
public class QuizzyApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizzyApplication.class, args);
	}

}
