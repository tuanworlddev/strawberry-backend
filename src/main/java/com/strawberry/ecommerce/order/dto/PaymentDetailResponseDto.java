package com.strawberry.ecommerce.order.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentDetailResponseDto {
    private UUID orderId;
    private String orderNumber;
    private String customerName;
    private String paymentStatus;
    private BigDecimal transferAmount;
    private LocalDateTime transferTime;
    private String receiptImageUrl;
    private LocalDateTime submittedAt;
    private BigDecimal orderTotal;
    private String payerName;
}
