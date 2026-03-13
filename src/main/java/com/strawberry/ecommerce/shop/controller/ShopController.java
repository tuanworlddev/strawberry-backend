package com.strawberry.ecommerce.shop.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.shop.dto.CreateShopRequest;
import com.strawberry.ecommerce.shop.dto.ShopResponseDto;
import com.strawberry.ecommerce.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Tag(name = "Shop Management", description = "Endpoints for Sellers to manage their shops")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Create a new shop (Requires APPROVED SELLER)")
    public ResponseEntity<ShopResponseDto> createShop(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateShopRequest request) {
        
        ShopResponseDto response = shopService.createShop(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
