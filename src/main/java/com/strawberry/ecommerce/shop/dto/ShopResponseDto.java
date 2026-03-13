package com.strawberry.ecommerce.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponseDto {
    private UUID shopId;
    private String name;
    private String slug;
    private String status;
}
