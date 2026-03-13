package com.strawberry.ecommerce.catalog.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductDetailResponseDto {
    private UUID id;
    private String slug;
    private String title;
    private String description;
    private String brand;
    private String categoryName;
    private List<String> images;
    private List<CharacteristicDto> characteristics;
    private List<VariantDto> variants;
    private ShopDto shop;

    @Data
    @Builder
    public static class CharacteristicDto {
        private String name;
        private String value;
    }

    @Data
    @Builder
    public static class VariantDto {
        private UUID id;
        private String techSize;
        private String wbSize;
        private BigDecimal basePrice;
        private BigDecimal discountPrice;
        private int stockQuantity;
        private boolean inStock;
    }

    @Data
    @Builder
    public static class ShopDto {
        private UUID id;
        private String name;
        private String slug;
    }
}
