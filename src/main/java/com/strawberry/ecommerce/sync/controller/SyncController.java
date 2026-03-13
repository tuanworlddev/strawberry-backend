package com.strawberry.ecommerce.sync.controller;

import com.strawberry.ecommerce.common.security.UserDetailsImpl;
import com.strawberry.ecommerce.sync.dto.*;
import com.strawberry.ecommerce.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @Operation(summary = "Trigger incremental catalog sync from Wildberries")
    public ResponseEntity<SyncJobResponseDto> triggerIncrementalSync(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @RequestBody @Valid SyncJobRequestDto request) {
        // The request object might already have the type set if coming from a specific client,
        // but we ensure it's incremental if not explicitly set or if we want to override.
        if (request.getSyncType() == null) {
            request.setSyncType(com.strawberry.ecommerce.sync.entity.SyncType.INCREMENTAL);
        }
        return ResponseEntity.ok(syncService.triggerSync(userDetails.getId(), shopId, request));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Update sync settings for a shop")
    public ResponseEntity<Void> updateSyncSettings(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @RequestBody @Valid SyncSettingsRequestDto request) {
        syncService.updateSyncSettings(userDetails.getId(), shopId, request.getSyncIntervalMinutes(), request.getIsSyncPaused());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get sync history for a shop")
    public ResponseEntity<List<SyncHistoryDto>> getSyncHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(syncService.getSyncHistory(userDetails.getId(), shopId, limit));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get sync health and statistics for a shop")
    public ResponseEntity<SyncHealthDto> getSyncHealth(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID shopId) {
        return ResponseEntity.ok(syncService.getSyncHealth(userDetails.getId(), shopId));
    }
}
