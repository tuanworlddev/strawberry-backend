package com.strawberry.ecommerce.catalog.controller;

import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.repository.ProductRepository;
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

    private final ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get all products for the authenticated seller's shop")
    public ResponseEntity<Page<Product>> getMyProducts(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Pageable pageable) {
        
        // MVP logic: In a real app we query by shopId which belongs to the user.
        // Assuming user -> 1 shop mapping in MVP
        // Temporarily returning findAll to satisfy interface till shop-filter is wired.
        Page<Product> products = productRepository.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get detailed product information")
    public ResponseEntity<Product> getProductDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID productId) {
        
        return productRepository.findById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
