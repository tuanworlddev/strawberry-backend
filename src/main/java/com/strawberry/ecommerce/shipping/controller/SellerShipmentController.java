package com.strawberry.ecommerce.shipping.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.shipping.dto.CreateShipmentRequestDto;
import com.strawberry.ecommerce.shipping.dto.ShipmentResponseDto;
import com.strawberry.ecommerce.shipping.entity.ShipmentStatus;
import com.strawberry.ecommerce.shipping.service.SellerShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.strawberry.ecommerce.shop.service.ShopOwnershipService;

@RestController
@RequestMapping("/api/v1/seller/shops/{shopId}")
@RequiredArgsConstructor
@Tag(name = "Seller Shipment", description = "Endpoints for sellers to manage shipments")
public class SellerShipmentController {

    private final SellerShipmentService shipmentService;
    private final ShopOwnershipService shopOwnershipService;

    @PostMapping("/orders/{orderId}/ship")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Create a shipment for an order (moves order to SHIPPING)")
    public ResponseEntity<ShipmentResponseDto> createShipment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateShipmentRequestDto requestDto) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(shipmentService.createShipment(shopId, orderId, requestDto));
    }

    @PutMapping("/shipments/{shipmentId}/status")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update shipment status (DELIVERED moves order to DELIVERED)")
    public ResponseEntity<ShipmentResponseDto> updateShipmentStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @PathVariable UUID shipmentId,
            @RequestParam ShipmentStatus newStatus) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(shopId, shipmentId, newStatus));
    }

    @GetMapping("/shipments")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get all shipments for seller's shop")
    public ResponseEntity<List<ShipmentResponseDto>> getShopShipments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId) {
        shopOwnershipService.validateAndGetShop(shopId, userDetails.getId());
        return ResponseEntity.ok(shipmentService.getShopShipments(shopId));
    }
}
