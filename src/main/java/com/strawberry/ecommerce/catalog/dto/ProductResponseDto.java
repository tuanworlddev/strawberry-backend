package com.strawberry.ecommerce.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductResponseDto {
    private UUID id;
    private String slug;
    private String title;
    private String mainImage;
    private String brand;
    private String shopName;
    private String shopSlug;
    private BigDecimal minPrice;
    private BigDecimal minDiscountPrice;
    private boolean inStock;
}
