package com.strawberry.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private int expiresIn;
    private UserDto user;

    @Data
    @AllArgsConstructor
    public static class UserDto {
        private UUID id;
        private String email;
        private String role;
    }
}
