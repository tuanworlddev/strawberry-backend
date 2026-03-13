package com.strawberry.ecommerce.wb.repository;

import com.strawberry.ecommerce.wb.entity.ShopWbIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopWbIntegrationRepository extends JpaRepository<ShopWbIntegration, UUID> {
    Optional<ShopWbIntegration> findByShopId(UUID shopId);
}
