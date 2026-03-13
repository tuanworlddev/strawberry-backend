package com.strawberry.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequestDto {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    @NotBlank(message = "Full name is required")
    private String customerName;
    
    @NotBlank(message = "Phone number is required")
    private String customerPhone;
    
    private String customerEmail;
    
    private String customerNote;
}
