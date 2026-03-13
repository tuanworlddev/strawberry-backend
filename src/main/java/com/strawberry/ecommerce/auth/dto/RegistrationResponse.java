package com.strawberry.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class RegistrationResponse {
    private UUID id;
    private String status;
    private String role;
    private SellerProfileDto sellerProfile;

    @Data
    @Builder
    public static class SellerProfileDto {
        private String approvalStatus;
    }
}
