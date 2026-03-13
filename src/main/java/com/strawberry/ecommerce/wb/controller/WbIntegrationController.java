package com.strawberry.ecommerce.wb.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.wb.dto.IntegrationResponseDto;
import com.strawberry.ecommerce.wb.dto.UpdateIntegrationRequest;
import com.strawberry.ecommerce.wb.service.WbIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shops/{shopId}/integration")
@RequiredArgsConstructor
@Tag(name = "WB Integrations", description = "Endpoints to manage WB API Keys")
public class WbIntegrationController {

    private final WbIntegrationService integrationService;

    @PutMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update Wildberries API Key for a Shop")
    public ResponseEntity<IntegrationResponseDto> updateIntegration(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @Valid @RequestBody UpdateIntegrationRequest request) {

        IntegrationResponseDto response = integrationService.updateIntegration(userDetails.getId(), shopId, request.getWbApiKey());
        return ResponseEntity.ok(response);
    }
}
