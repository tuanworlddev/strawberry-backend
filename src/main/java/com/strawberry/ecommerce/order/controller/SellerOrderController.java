package com.strawberry.ecommerce.order.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.order.dto.OrderResponseDto;
import com.strawberry.ecommerce.order.entity.OrderStatus;
import com.strawberry.ecommerce.order.entity.PaymentStatus;
import com.strawberry.ecommerce.order.dto.PaymentDetailResponseDto;
import com.strawberry.ecommerce.order.service.SellerOrderService;
import com.strawberry.ecommerce.shop.service.ShopOwnershipService;
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
@RequestMapping("/api/v1/seller/shops/{shopId}")
@RequiredArgsConstructor
@Tag(name = "Seller Orders", description = "Order management for sellers")
public class SellerOrderController {

    private final SellerOrderService orderService;
    private final ShopOwnershipService shopOwnershipService;

    @GetMapping("/orders")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get all orders for the shop")
    public ResponseEntity<List<OrderResponseDto>> getOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.getShopOrders(shopId, status, paymentStatus));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get all pending payments for the shop")
    public ResponseEntity<List<PaymentDetailResponseDto>> getPayments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.getDetailedPayments(shopId));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get detailed order information")
    public ResponseEntity<OrderResponseDto> getOrderDetails(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID orderId) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.getOrderDetails(shopId, orderId));
    }

    @PostMapping("/orders/{orderId}/payment/approve")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Approve payment for an order")
    public ResponseEntity<OrderResponseDto> approvePayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID orderId) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.approvePayment(shopId, orderId));
    }

    @PostMapping("/orders/{orderId}/payment/reject")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Reject payment for an order")
    public ResponseEntity<OrderResponseDto> rejectPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID orderId) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.rejectPayment(shopId, orderId));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update order fulfillment status")
    public ResponseEntity<OrderResponseDto> updateStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(orderService.updateFulfillmentStatus(shopId, orderId, status));
    }
}
