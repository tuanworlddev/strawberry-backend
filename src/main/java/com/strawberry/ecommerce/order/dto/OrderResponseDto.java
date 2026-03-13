package com.strawberry.ecommerce.order.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponseDto {
    private UUID id;
    private String orderNumber;
    private UUID shopId;
    private String shopName;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDto> items;
    private String receiptImageUrl; // Optional latest receipt image
}
