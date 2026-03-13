package com.strawberry.ecommerce.catalog.controller;

import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.repository.ProductRepository;
import com.strawberry.ecommerce.catalog.dto.CatalogFiltersDto;
import com.strawberry.ecommerce.catalog.dto.ProductDetailResponseDto;
import com.strawberry.ecommerce.catalog.dto.ProductResponseDto;
import com.strawberry.ecommerce.catalog.service.CatalogSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/catalog")
@RequiredArgsConstructor
@Tag(name = "Storefront Catalog", description = "Public endpoints for browsing the catalog")
public class CatalogPublicController {

    private final CatalogSearchService searchService;

    @GetMapping("/search")
    @Operation(summary = "Multi-faceted product search")
    public ResponseEntity<Page<ProductResponseDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID shopId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            Pageable pageable) {
        
        return ResponseEntity.ok(searchService.search(q, shopId, category, brand, minPrice, maxPrice, inStock, pageable));
    }

    @GetMapping("/products/{slug}")
    @Operation(summary = "Get detailed product information by SEO slug")
    public ResponseEntity<ProductDetailResponseDto> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(searchService.getProductBySlug(slug));
    }

    @GetMapping("/filters")
    @Operation(summary = "Get unique categories and brands from the currently visible catalog")
    public ResponseEntity<CatalogFiltersDto> getFilters() {
        return ResponseEntity.ok(searchService.getFilters());
    }
}
