package com.strawberry.ecommerce.catalog.repository;

import com.strawberry.ecommerce.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByShopIdAndWbNmId(UUID shopId, Long wbNmId);
}
