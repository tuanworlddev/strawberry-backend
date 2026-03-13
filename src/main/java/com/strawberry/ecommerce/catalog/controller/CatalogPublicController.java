package com.strawberry.ecommerce.catalog.controller;

import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
@Tag(name = "Storefront Catalog", description = "Public endpoints for browsing the catalog")
public class CatalogPublicController {

    private final ProductRepository productRepository;

    @GetMapping
    @Operation(summary = "List all active public products")
    public ResponseEntity<Page<Product>> listProducts(Pageable pageable) {
        // MVP: List all products. Future: filter by is_active and visibility='ACTIVE'
        Page<Product> products = productRepository.findAll(pageable);
        return ResponseEntity.ok(products);
    }
}
