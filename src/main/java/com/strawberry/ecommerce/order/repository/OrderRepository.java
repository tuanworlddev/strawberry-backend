package com.strawberry.ecommerce.order.repository;

import com.strawberry.ecommerce.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<Order> findByIdAndCustomerId(UUID id, UUID customerId);
    Optional<Order> findByIdAndShopId(UUID id, UUID shopId);
}
