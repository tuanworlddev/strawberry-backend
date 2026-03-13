package com.strawberry.ecommerce.sync.service;

import com.strawberry.ecommerce.audit.service.AuditService;
import com.strawberry.ecommerce.common.exception.ApiException;
import com.strawberry.ecommerce.shop.entity.Shop;
import com.strawberry.ecommerce.shop.repository.ShopRepository;
import com.strawberry.ecommerce.sync.dto.SyncJobMessage;
import com.strawberry.ecommerce.sync.dto.SyncJobRequestDto;
import com.strawberry.ecommerce.sync.dto.SyncJobResponseDto;
import com.strawberry.ecommerce.sync.entity.SyncJob;
import com.strawberry.ecommerce.sync.entity.SyncStatus;
import com.strawberry.ecommerce.sync.repository.SyncJobRepository;
import com.strawberry.ecommerce.wb.entity.ShopWbIntegration;
import com.strawberry.ecommerce.wb.repository.ShopWbIntegrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncService {

    private final ShopRepository shopRepository;
    private final ShopWbIntegrationRepository integrationRepository;
    private final SyncJobRepository syncJobRepository;
    private final SyncMessageProducer syncMessageProducer;
    private final AuditService auditService;

    @Transactional
    public SyncJobResponseDto triggerSync(UUID userId, UUID shopId, SyncJobRequestDto request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ApiException("Shop not found", HttpStatus.NOT_FOUND));

        if (!shop.getSellerProfile().getUser().getId().equals(userId)) {
            throw new ApiException("You do not have permission to trigger sync for this shop", HttpStatus.FORBIDDEN);
        }

        ShopWbIntegration integration = integrationRepository.findByShopId(shopId)
                .orElseThrow(() -> new ApiException("Wildberries Integration not configured for this shop", HttpStatus.BAD_REQUEST));

        if (!Boolean.TRUE.equals(integration.getIsActive())) {
            throw new ApiException("Wildberries Integration is disabled", HttpStatus.BAD_REQUEST);
        }

        SyncJob syncJob = new SyncJob();
        syncJob.setShop(shop);
        // Mock completion (to be replaced with actual sync logic)
        syncJob.setStatus(SyncStatus.SUCCESS);
        syncJob.setFinishedAt(ZonedDateTime.now());
        syncJob.setSyncType(request.getSyncType());
        syncJob.setStartedAt(ZonedDateTime.now());
        
        SyncJob savedJob = syncJobRepository.save(syncJob);

        SyncJobMessage message = SyncJobMessage.builder()
                .syncJobId(savedJob.getId())
                .shopId(shop.getId())
                .integrationId(integration.getId())
                .syncType(savedJob.getSyncType())
                .cursor(integration.getLastCursorNmId())
                .attemptNumber(1)
                .build();

        syncMessageProducer.publishSyncJob(message);

        auditService.logAction(
                "SYNC_JOB_TRIGGERED",
                "SyncJob",
                savedJob.getId().toString(),
                null,
                "{\"syncType\":\"" + savedJob.getSyncType().name() + "\"}"
        );

        return SyncJobResponseDto.builder()
                .syncJobId(savedJob.getId())
                .shopId(shop.getId())
                .syncType(savedJob.getSyncType())
                .status(savedJob.getStatus())
                .startedAt(savedJob.getStartedAt())
                .build();
    }
}
