package com.strawberry.ecommerce.order.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.order.dto.OrderResponseDto;
import com.strawberry.ecommerce.order.entity.OrderStatus;
import com.strawberry.ecommerce.order.entity.PaymentStatus;
import com.strawberry.ecommerce.order.service.SellerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/orders")
@RequiredArgsConstructor
@Tag(name = "Seller Order Controller", description = "Endpoints for seller order fulfillment and payment checking")
@PreAuthorize("hasRole('SELLER')")
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @GetMapping
    @Operation(summary = "Get shop orders with optional filters")
    public ResponseEntity<List<OrderResponseDto>> getShopOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        return ResponseEntity.ok(sellerOrderService.getShopOrders(userDetails.getId(), status, paymentStatus));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get specific order details")
    public ResponseEntity<OrderResponseDto> getOrderDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(sellerOrderService.getOrderDetails(userDetails.getId(), orderId));
    }

    @PostMapping("/{orderId}/payment/approve")
    @Operation(summary = "Approve payment confirmation")
    public ResponseEntity<OrderResponseDto> approvePayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(sellerOrderService.approvePayment(userDetails.getId(), orderId));
    }

    @PostMapping("/{orderId}/payment/reject")
    @Operation(summary = "Reject payment confirmation")
    public ResponseEntity<OrderResponseDto> rejectPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(sellerOrderService.rejectPayment(userDetails.getId(), orderId));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update fulfillment status")
    public ResponseEntity<OrderResponseDto> updateFulfillmentStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestParam OrderStatus newStatus) {
        return ResponseEntity.ok(sellerOrderService.updateFulfillmentStatus(userDetails.getId(), orderId, newStatus));
    }
}
