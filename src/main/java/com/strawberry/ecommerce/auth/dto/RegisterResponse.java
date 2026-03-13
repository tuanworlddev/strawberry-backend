package com.strawberry.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String role;
    private String status;
    private String approvalStatus;
}
