package com.strawberry.ecommerce.catalog.service;

import com.strawberry.ecommerce.catalog.dto.CatalogFiltersDto;
import com.strawberry.ecommerce.catalog.dto.ProductDetailResponseDto;
import com.strawberry.ecommerce.catalog.dto.ProductResponseDto;
import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.entity.ProductVariant;
import com.strawberry.ecommerce.catalog.repository.ProductRepository;
import com.strawberry.ecommerce.common.exception.ApiException;
import org.springframework.http.HttpStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogSearchService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> search(
            String query,
            UUID shopId,
            String category,
            String brand,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStockOnly,
            Pageable pageable) {

        Specification<Product> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Core Visibility Rules
            predicates.add(cb.equal(root.get("visibility"), "ACTIVE"));
            predicates.add(cb.isNotNull(root.get("seoSlug")));
            predicates.add(cb.isNotEmpty(root.get("variants")));

            // Join variants for price and stock filters
            Join<Product, ProductVariant> variants = root.join("variants");
            predicates.add(cb.isTrue(variants.get("isActive")));

            // 2. Filters
            if (query != null && !query.isEmpty()) {
                String lq = "%" + query.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("localTitle")), lq),
                        cb.like(cb.lower(root.get("title")), lq),
                        cb.like(cb.lower(root.get("brand")), lq)
                ));
            }

            if (shopId != null) {
                predicates.add(cb.equal(root.get("shop").get("id"), shopId));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("categoryName"), category));
            }

            if (brand != null) {
                predicates.add(cb.equal(root.get("brand"), brand));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(variants.get("basePrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(variants.get("basePrice"), maxPrice));
            }

            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(cb.greaterThan(variants.get("stockQuantity"), 0));
            }

            // Ensure we only get distinct products because of variant join
            q.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponseDto getProductBySlug(String slug) {
        Product product = productRepository.findBySeoSlug(slug)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        if (!"ACTIVE".equals(product.getVisibility())) {
             throw new ApiException("Product not found", HttpStatus.NOT_FOUND);
        }

        return mapToDetailResponse(product);
    }

    @Transactional(readOnly = true)
    public CatalogFiltersDto getFilters() {
        List<Product> products = productRepository.findAll();
        
        List<String> categories = products.stream()
                .filter(p -> "ACTIVE".equals(p.getVisibility()) && p.getSeoSlug() != null && !p.getVariants().isEmpty())
                .map(Product::getCategoryName)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> brands = products.stream()
                .filter(p -> "ACTIVE".equals(p.getVisibility()) && p.getSeoSlug() != null && !p.getVariants().isEmpty())
                .map(Product::getBrand)
                .filter(b -> b != null && !b.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return CatalogFiltersDto.builder()
                .categories(categories)
                .brands(brands)
                .build();
    }

    private ProductResponseDto mapToResponse(Product p) {
        BigDecimal minPrice = p.getVariants().stream()
                .filter(ProductVariant::getIsActive)
                .map(v -> v.getDiscountPrice() != null ? v.getDiscountPrice() : v.getBasePrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        boolean inStock = p.getVariants().stream()
                .filter(ProductVariant::getIsActive)
                .anyMatch(v -> v.getStockQuantity() > 0);

        String mainImg = p.getImages().isEmpty() ? null : p.getImages().get(0).getWbUrl();

        return ProductResponseDto.builder()
                .id(p.getId())
                .slug(p.getSeoSlug())
                .title(p.getLocalTitle() != null ? p.getLocalTitle() : p.getTitle())
                .brand(p.getBrand())
                .mainImage(mainImg)
                .shopName(p.getShop().getName())
                .shopSlug(p.getShop().getSlug())
                .minPrice(minPrice)
                .inStock(inStock)
                .build();
    }

    private ProductDetailResponseDto mapToDetailResponse(Product p) {
        return ProductDetailResponseDto.builder()
                .id(p.getId())
                .slug(p.getSeoSlug())
                .title(p.getLocalTitle() != null ? p.getLocalTitle() : p.getTitle())
                .description(p.getLocalDescription() != null ? p.getLocalDescription() : p.getDescription())
                .brand(p.getBrand())
                .categoryName(p.getCategoryName())
                .images(p.getImages().stream().map(img -> img.getWbUrl()).collect(Collectors.toList()))
                .characteristics(p.getCharacteristics().stream()
                        .map(c -> ProductDetailResponseDto.CharacteristicDto.builder()
                                .name(c.getName())
                                .value(c.getNormalizedText())
                                .build())
                        .collect(Collectors.toList()))
                .variants(p.getVariants().stream()
                        .filter(ProductVariant::getIsActive)
                        .map(v -> ProductDetailResponseDto.VariantDto.builder()
                                .id(v.getId())
                                .techSize(v.getTechSize())
                                .wbSize(v.getWbSize())
                                .basePrice(v.getBasePrice())
                                .discountPrice(v.getDiscountPrice())
                                .stockQuantity(v.getStockQuantity())
                                .inStock(v.getStockQuantity() > 0)
                                .build())
                        .collect(Collectors.toList()))
                .shop(ProductDetailResponseDto.ShopDto.builder()
                        .id(p.getShop().getId())
                        .name(p.getShop().getName())
                        .slug(p.getShop().getSlug())
                        .build())
                .build();
    }
}
