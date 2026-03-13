package com.strawberry.ecommerce.sync.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.sync.dto.SyncJobRequestDto;
import com.strawberry.ecommerce.sync.dto.SyncJobResponseDto;
import com.strawberry.ecommerce.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seller/shops/{shopId}/sync")
@RequiredArgsConstructor
@Tag(name = "Wildberries Sync", description = "Endpoints to trigger catalog synchronizations")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/full")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Trigger a full Wildberries sync job")
    public ResponseEntity<SyncJobResponseDto> triggerFullSync(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId) {

        SyncJobRequestDto request = new SyncJobRequestDto();
        request.setSyncType(com.strawberry.ecommerce.sync.entity.SyncType.FULL);
        SyncJobResponseDto response = syncService.triggerSync(userDetails.getId(), shopId, request);
        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Trigger an incremental update Wildberries sync job")
    public ResponseEntity<SyncJobResponseDto> triggerUpdateSync(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId) {

        SyncJobRequestDto request = new SyncJobRequestDto();
        request.setSyncType(com.strawberry.ecommerce.sync.entity.SyncType.INCREMENTAL);
        SyncJobResponseDto response = syncService.triggerSync(userDetails.getId(), shopId, request);
        return ResponseEntity.accepted().body(response);
    }
}
