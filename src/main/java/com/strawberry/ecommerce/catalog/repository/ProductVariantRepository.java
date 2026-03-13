package com.strawberry.ecommerce.catalog.repository;

import com.strawberry.ecommerce.catalog.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    Optional<ProductVariant> findByProductIdAndChrtId(UUID productId, Long chrtId);
}
