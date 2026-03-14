package com.strawberry.ecommerce.seller.service;

import com.strawberry.ecommerce.catalog.repository.ProductRepository;
import com.strawberry.ecommerce.order.dto.OrderResponseDto;
import com.strawberry.ecommerce.order.entity.Order;
import com.strawberry.ecommerce.order.entity.OrderStatus;
import com.strawberry.ecommerce.order.entity.PaymentStatus;
import com.strawberry.ecommerce.order.repository.OrderRepository;
import com.strawberry.ecommerce.order.repository.PaymentConfirmationRepository;
import com.strawberry.ecommerce.seller.dto.SellerDashboardResponseDto;
import com.strawberry.ecommerce.shop.entity.Shop;
import com.strawberry.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentConfirmationRepository paymentConfirmationRepository;

    @Transactional(readOnly = true)
    public SellerDashboardResponseDto getDashboardStats(UUID shopId) {
        long totalProducts = productRepository.countByShopId(shopId);
        long totalOrders = orderRepository.countByShopId(shopId);
        long pendingPayments = orderRepository.countByShopIdAndPaymentStatus(shopId, PaymentStatus.WAITING_CONFIRMATION);
        
        // Pending shipments: Approved payment but not yet shipped (NEW, ASSEMBLING)
        long pendingShipments = orderRepository.countByShopIdAndStatusIn(
                shopId, 
                Arrays.asList(OrderStatus.NEW, OrderStatus.ASSEMBLING)
        );

        List<Order> topOrders = orderRepository.findByShopIdOrderByCreatedAtDesc(shopId, PageRequest.of(0, 5));

        return SellerDashboardResponseDto.builder()
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .pendingPayments(pendingPayments)
                .pendingShipments(pendingShipments)
                .recentOrders(topOrders.stream().map(this::mapToResponseDto).collect(Collectors.toList()))
                .build();
    }

    private OrderResponseDto mapToResponseDto(Order order) {
        // Shared mapping logic (Ideally consolidated in a mapper)
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalAmount(order.getTotalAmount())
                .customerName(order.getCustomerName())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
