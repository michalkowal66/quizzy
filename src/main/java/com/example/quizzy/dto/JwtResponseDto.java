package com.example.quizzy.dto;

import lombok.Getter;

@Getter
public class JwtResponseDto {
    private final String token;
    private final String type = "Bearer";

    public JwtResponseDto(String token) {
        this.token = token;
    }
}
