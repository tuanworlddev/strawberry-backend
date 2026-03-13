package com.strawberry.ecommerce.order.service;

import com.strawberry.ecommerce.catalog.entity.ProductVariant;
import com.strawberry.ecommerce.catalog.repository.ProductVariantRepository;
import com.strawberry.ecommerce.common.exception.ApiException;
import com.strawberry.ecommerce.order.dto.OrderItemResponseDto;
import com.strawberry.ecommerce.order.dto.OrderResponseDto;
import com.strawberry.ecommerce.order.entity.Order;
import com.strawberry.ecommerce.order.entity.OrderStatus;
import com.strawberry.ecommerce.order.entity.PaymentConfirmation;
import com.strawberry.ecommerce.order.entity.PaymentStatus;
import com.strawberry.ecommerce.order.repository.OrderRepository;
import com.strawberry.ecommerce.order.repository.PaymentConfirmationRepository;
import com.strawberry.ecommerce.shop.entity.Shop;
import com.strawberry.ecommerce.shop.repository.ShopRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final PaymentConfirmationRepository paymentConfirmationRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getShopOrders(UUID sellerId, OrderStatus status, PaymentStatus paymentStatus) {
        Shop shop = shopRepository.findBySellerProfileUserId(sellerId)
                .orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));

        List<Order> orders = orderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("shop").get("id"), shop.getId()));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        return orders.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderDetails(UUID sellerId, UUID orderId) {
        return mapToResponse(getOrderForSeller(sellerId, orderId));
    }

    @Transactional
    public OrderResponseDto approvePayment(UUID sellerId, UUID orderId) {
        Order order = getOrderForSeller(sellerId, orderId);

        if (order.getPaymentStatus() != PaymentStatus.WAITING_CONFIRMATION) {
            throw new ApiException("Order is not currently waiting for payment confirmation", HttpStatus.BAD_REQUEST);
        }

        order.setPaymentStatus(PaymentStatus.APPROVED);

        paymentConfirmationRepository.findFirstByOrderIdOrderBySubmittedAtDesc(order.getId())
                .ifPresent(confirmation -> {
                    confirmation.setReviewedAt(LocalDateTime.now());
                    paymentConfirmationRepository.save(confirmation);
                });

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto rejectPayment(UUID sellerId, UUID orderId) {
        Order order = getOrderForSeller(sellerId, orderId);

        if (order.getPaymentStatus() != PaymentStatus.WAITING_CONFIRMATION) {
            throw new ApiException("Order is not currently waiting for payment confirmation", HttpStatus.BAD_REQUEST);
        }

        order.setPaymentStatus(PaymentStatus.REJECTED);

        paymentConfirmationRepository.findFirstByOrderIdOrderBySubmittedAtDesc(order.getId())
                .ifPresent(confirmation -> {
                    confirmation.setReviewedAt(LocalDateTime.now());
                    paymentConfirmationRepository.save(confirmation);
                });

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto updateFulfillmentStatus(UUID sellerId, UUID orderId, OrderStatus newStatus) {
        Order order = getOrderForSeller(sellerId, orderId);
        OrderStatus currentStatus = order.getStatus();

        if (newStatus == OrderStatus.ASSEMBLING) {
            if (currentStatus != OrderStatus.NEW) {
                throw new ApiException("Can only transition to ASSEMBLING from NEW", HttpStatus.BAD_REQUEST);
            }
            if (order.getPaymentStatus() != PaymentStatus.APPROVED) {
                throw new ApiException("Cannot assemble order until payment is APPROVED", HttpStatus.BAD_REQUEST);
            }
        } 
        else if (newStatus == OrderStatus.SHIPPING) {
            if (currentStatus != OrderStatus.ASSEMBLING) {
                throw new ApiException("Can only transition to SHIPPING from ASSEMBLING", HttpStatus.BAD_REQUEST);
            }
        } 
        else if (newStatus == OrderStatus.DELIVERED) {
            if (currentStatus != OrderStatus.SHIPPING) {
                throw new ApiException("Can only transition to DELIVERED from SHIPPING", HttpStatus.BAD_REQUEST);
            }
            order.getItems().forEach(item -> {
                ProductVariant variant = item.getVariant();
                if (variant != null) {
                    variant.setReservedStock(Math.max(0, variant.getReservedStock() - item.getQuantity()));
                    variantRepository.save(variant);
                }
            });
        } 
        else if (newStatus == OrderStatus.CANCELLED) {
            if (currentStatus == OrderStatus.SHIPPING || currentStatus == OrderStatus.DELIVERED) {
                throw new ApiException("Cannot cancel orders that are already shipped or delivered", HttpStatus.BAD_REQUEST);
            }
            order.getItems().forEach(item -> {
                ProductVariant variant = item.getVariant();
                if (variant != null) {
                    variant.setReservedStock(Math.max(0, variant.getReservedStock() - item.getQuantity()));
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    variantRepository.save(variant);
                }
            });
        } 
        else {
            if (currentStatus == newStatus) {
                return mapToResponse(order);
            }
            throw new ApiException("Invalid status transition from " + currentStatus + " to " + newStatus, HttpStatus.BAD_REQUEST);
        }

        order.setStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }

    private Order getOrderForSeller(UUID sellerId, UUID orderId) {
        Shop shop = shopRepository.findBySellerProfileUserId(sellerId)
                .orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));

        return orderRepository.findByIdAndShopId(orderId, shop.getId())
                .orElseThrow(() -> new ApiException("Order not found or does not belong to your shop", HttpStatus.FORBIDDEN));
    }

    private OrderResponseDto mapToResponse(Order order) {
        String receiptUrl = paymentConfirmationRepository.findFirstByOrderIdOrderBySubmittedAtDesc(order.getId())
                .map(PaymentConfirmation::getReceiptImageUrl)
                .orElse(null);

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .shopId(order.getShop().getId())
                .shopName(order.getShop().getName())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .customerNote(order.getCustomerNote())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .receiptImageUrl(receiptUrl)
                .items(order.getItems().stream().map(item -> OrderItemResponseDto.builder()
                        .id(item.getId())
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .productTitleSnapshot(item.getProductTitleSnapshot())
                        .productSlugSnapshot(item.getProductSlugSnapshot())
                        .variantAttributesSnapshot(item.getVariantAttributesSnapshot())
                        .productImageSnapshot(item.getProductImageSnapshot())
                        .wbNmIdSnapshot(item.getWbNmIdSnapshot())
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
