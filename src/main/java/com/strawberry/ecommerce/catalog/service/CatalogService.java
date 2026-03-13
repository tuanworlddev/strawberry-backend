package com.strawberry.ecommerce.catalog.service;

import com.strawberry.ecommerce.catalog.dto.ProductMetadataRequestDto;
import com.strawberry.ecommerce.catalog.dto.VariantInventoryRequestDto;
import com.strawberry.ecommerce.catalog.dto.VariantPricingRequestDto;
import com.strawberry.ecommerce.catalog.entity.Product;
import com.strawberry.ecommerce.catalog.entity.ProductVariant;
import com.strawberry.ecommerce.catalog.repository.ProductRepository;
import com.strawberry.ecommerce.catalog.repository.ProductVariantRepository;
import com.strawberry.ecommerce.common.exception.ApiException;
import com.strawberry.ecommerce.catalog.dto.ProductResponseDto;
import com.strawberry.ecommerce.shop.entity.Shop;
import com.strawberry.ecommerce.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ShopRepository shopRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getMyProducts(UUID userId, Pageable pageable) {
        Shop shop = findShopByUserId(userId);
        return productRepository.findByShopId(shop.getId(), pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Product getProductDetail(UUID userId, UUID productId) {
        Shop shop = findShopByUserId(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException("Product not found", HttpStatus.NOT_FOUND));

        if (!product.getShop().getId().equals(shop.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        return product;
    }

    @Transactional
    public void updateProductMetadata(UUID userId, UUID productId, ProductMetadataRequestDto request) {
        Product product = getProductDetail(userId, productId);

        if (request.getLocalTitle() != null) product.setLocalTitle(request.getLocalTitle());
        if (request.getLocalDescription() != null) product.setLocalDescription(request.getLocalDescription());
        if (request.getVisibility() != null) product.setVisibility(request.getVisibility());
        if (request.getSlugOverride() != null) product.setSeoSlug(request.getSlugOverride());

        productRepository.save(product);
    }

    @Transactional
    public void updateVariantPricing(UUID userId, UUID variantId, VariantPricingRequestDto request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ApiException("Variant not found", HttpStatus.NOT_FOUND));

        checkAccess(userId, variant.getProduct());

        if (request.getBasePrice() != null) variant.setBasePrice(request.getBasePrice());
        if (request.getDiscountPrice() != null) variant.setDiscountPrice(request.getDiscountPrice());

        variantRepository.save(variant);
    }

    @Transactional
    public void updateVariantInventory(UUID userId, UUID variantId, VariantInventoryRequestDto request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ApiException("Variant not found", HttpStatus.NOT_FOUND));

        checkAccess(userId, variant.getProduct());

        if (request.getStockQuantity() != null) {
            if (request.getStockQuantity() < 0) {
                throw new ApiException("Stock quantity cannot be negative", HttpStatus.BAD_REQUEST);
            }
            variant.setStockQuantity(request.getStockQuantity());
        }

        variantRepository.save(variant);
    }

    private Shop findShopByUserId(UUID userId) {
        // Find shop by searching for the seller profile of the user
        // Assuming SellerProfile is linked to User.
        // For MVP, we'll find shop where sellerProfile.user.id = userId
        return shopRepository.findAll().stream()
                .filter(s -> s.getSellerProfile().getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ApiException("Shop not found for user", HttpStatus.NOT_FOUND));
    }

    private void checkAccess(UUID userId, Product product) {
        Shop shop = findShopByUserId(userId);
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
    }

    private ProductResponseDto mapToResponse(Product p) {
        BigDecimal minPrice = p.getVariants().stream()
                .filter(ProductVariant::getIsActive)
                .map(v -> v.getDiscountPrice() != null ? v.getDiscountPrice() : v.getBasePrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        boolean inStock = p.getVariants().stream()
                .filter(ProductVariant::getIsActive)
                .anyMatch(v -> v.getStockQuantity() > 0);

        String mainImg = p.getImages().isEmpty() ? null : p.getImages().get(0).getWbUrl();

        return ProductResponseDto.builder()
                .id(p.getId())
                .slug(p.getSeoSlug())
                .title(p.getLocalTitle() != null ? p.getLocalTitle() : p.getTitle())
                .brand(p.getBrand())
                .mainImage(mainImg)
                .shopName(p.getShop().getName())
                .shopSlug(p.getShop().getSlug())
                .minPrice(minPrice)
                .inStock(inStock)
                .build();
    }
}
