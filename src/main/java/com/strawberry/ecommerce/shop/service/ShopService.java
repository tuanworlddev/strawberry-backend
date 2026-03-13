package com.strawberry.ecommerce.shop.service;

import com.strawberry.ecommerce.audit.service.AuditService;
import com.strawberry.ecommerce.common.exception.ApiException;
import com.strawberry.ecommerce.shop.dto.CreateShopRequest;
import com.strawberry.ecommerce.shop.dto.ShopResponseDto;
import com.strawberry.ecommerce.shop.entity.Shop;
import com.strawberry.ecommerce.shop.entity.ShopStatus;
import com.strawberry.ecommerce.shop.repository.ShopRepository;
import com.strawberry.ecommerce.seller.entity.SellerProfile;
import com.strawberry.ecommerce.seller.repository.SellerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final AuditService auditService;

    @Transactional
    public ShopResponseDto createShop(UUID userId, CreateShopRequest request) {
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Seller Profile not found", HttpStatus.NOT_FOUND));

        if (!"APPROVED".equals(sellerProfile.getApprovalStatus())) {
            throw new ApiException("Only APPROVED sellers can create a shop", HttpStatus.FORBIDDEN);
        }

        if (shopRepository.existsBySlug(request.getSlug())) {
            throw new ApiException("Shop slug is already taken", HttpStatus.BAD_REQUEST);
        }

        // Just an MVP check: allow only 1 shop per seller profile
        if (shopRepository.findBySellerProfileId(sellerProfile.getId()).isPresent()) {
            throw new ApiException("Seller already has a shop", HttpStatus.BAD_REQUEST);
        }

        Shop shop = new Shop();
        shop.setSellerProfile(sellerProfile);
        shop.setName(request.getName());
        shop.setSlug(request.getSlug());
        shop.setLogoUrl(request.getLogoUrl());
        shop.setContactInfo(request.getContactInfo());
        shop.setBankName(request.getBankName());
        shop.setAccountNumber(request.getAccountNumber());
        shop.setAccountHolderName(request.getAccountHolderName());
        shop.setBik(request.getBik());
        shop.setCorrespondentAccount(request.getCorrespondentAccount());
        shop.setPaymentInstructions(request.getPaymentInstructions());
        shop.setStatus(ShopStatus.DRAFT);

        Shop savedShop = shopRepository.save(shop);

        auditService.logAction(
                "SHOP_CREATED",
                "Shop",
                savedShop.getId().toString(),
                null,
                "{\"name\":\"" + savedShop.getName() + "\", \"slug\":\"" + savedShop.getSlug() + "\"}"
        );

        return ShopResponseDto.builder()
                .shopId(savedShop.getId())
                .name(savedShop.getName())
                .slug(savedShop.getSlug())
                .status(savedShop.getStatus().name())
                .build();
    }

    public List<ShopResponseDto> getMyShops(UUID userId) {
        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Seller Profile not found", HttpStatus.NOT_FOUND));

        return shopRepository.findBySellerProfileId(sellerProfile.getId()).stream()
                .map(shop -> ShopResponseDto.builder()
                        .shopId(shop.getId())
                        .name(shop.getName())
                        .slug(shop.getSlug())
                        .status(shop.getStatus().name())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
