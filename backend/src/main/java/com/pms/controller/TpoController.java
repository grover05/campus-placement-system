package com.pms.controller;

import com.pms.dto.*;
import com.pms.model.DriveStatus;
import com.pms.service.TpoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tpo")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TPO')")
@Tag(name = "TPO", description = "Endpoints for the placement officer role")
public class TpoController {

    private final TpoService tpoService;

    @PostMapping("/drives")
    @Operation(summary = "Post a new placement drive with eligibility criteria")
    public ResponseEntity<DriveResponse> createDrive(Authentication authentication, @Valid @RequestBody DriveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tpoService.createDrive(authentication.getName(), request));
    }

    @GetMapping("/drives")
    @Operation(summary = "List all drives")
    public ResponseEntity<List<DriveResponse>> getAllDrives() {
        return ResponseEntity.ok(tpoService.getAllDrives());
    }

    @PatchMapping("/drives/{driveId}/status")
    @Operation(summary = "Open or close a drive")
    public ResponseEntity<DriveResponse> updateDriveStatus(@PathVariable Long driveId, @RequestParam DriveStatus status) {
        return ResponseEntity.ok(tpoService.updateDriveStatus(driveId, status));
    }

    @GetMapping("/drives/{driveId}/applications")
    @Operation(summary = "List all applications (applicants) for a drive")
    public ResponseEntity<List<ApplicationResponse>> getApplications(@PathVariable Long driveId) {
        return ResponseEntity.ok(tpoService.getApplicationsForDrive(driveId));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @Operation(summary = "Approve / shortlist / reject a student's application")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(@PathVariable Long applicationId,
                                                                        @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(tpoService.updateApplicationStatus(applicationId, request.getStatus()));
    }

    @GetMapping(value = "/drives/{driveId}/export", produces = "text/csv")
    @Operation(summary = "Export the shortlisted students for a drive as CSV")
    public ResponseEntity<byte[]> exportShortlist(@PathVariable Long driveId) {
        byte[] csv = tpoService.exportShortlist(driveId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"shortlist-drive-" + driveId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
