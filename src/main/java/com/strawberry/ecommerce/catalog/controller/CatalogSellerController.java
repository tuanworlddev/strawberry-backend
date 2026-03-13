package com.strawberry.ecommerce.catalog.controller;

import com.strawberry.ecommerce.catalog.dto.ProductMetadataRequestDto;
import com.strawberry.ecommerce.catalog.dto.VariantInventoryRequestDto;
import com.strawberry.ecommerce.catalog.dto.VariantPricingRequestDto;
import com.strawberry.ecommerce.catalog.dto.ProductResponseDto;
import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.service.CatalogService;
import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/products")
@RequiredArgsConstructor
@Tag(name = "Seller Catalog", description = "Endpoints for sellers to manage their synced products")
public class CatalogSellerController {

    private final CatalogService catalogService;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get all products for the authenticated seller's shop")
    public ResponseEntity<Page<ProductResponseDto>> getMyProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        return ResponseEntity.ok(catalogService.getMyProducts(userDetails.getId(), pageable));
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get detailed product information")
    public ResponseEntity<Product> getProductDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID productId) {
        return ResponseEntity.ok(catalogService.getProductDetail(userDetails.getId(), productId));
    }

    @PutMapping("/{productId}/metadata")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update product metadata (local title, description, visibility)")
    public ResponseEntity<Void> updateMetadata(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID productId,
            @RequestBody ProductMetadataRequestDto request) {
        catalogService.updateProductMetadata(userDetails.getId(), productId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/variants/{variantId}/pricing")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update variant pricing")
    public ResponseEntity<Void> updatePricing(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID variantId,
            @RequestBody VariantPricingRequestDto request) {
        catalogService.updateVariantPricing(userDetails.getId(), variantId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/variants/{variantId}/inventory")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update variant inventory")
    public ResponseEntity<Void> updateInventory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID variantId,
            @RequestBody VariantInventoryRequestDto request) {
        catalogService.updateVariantInventory(userDetails.getId(), variantId, request);
        return ResponseEntity.noContent().build();
    }
}
