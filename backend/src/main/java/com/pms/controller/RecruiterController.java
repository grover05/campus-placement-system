package com.pms.controller;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.ApplicationStatusUpdateRequest;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.service.RecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
@Tag(name = "Recruiter", description = "Endpoints for the recruiter role")
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping("/drives")
    @Operation(summary = "List drives associated with my company")
    public ResponseEntity<List<DriveResponse>> getMyDrives(Authentication authentication) {
        return ResponseEntity.ok(recruiterService.getMyDrives(authentication.getName()));
    }

    @GetMapping("/drives/{driveId}/applicants")
    @Operation(summary = "List applicants for one of my drives")
    public ResponseEntity<List<ApplicationResponse>> getApplicants(Authentication authentication, @PathVariable Long driveId) {
        return ResponseEntity.ok(recruiterService.getApplicantsForDrive(authentication.getName(), driveId));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @Operation(summary = "Mark an applicant as shortlisted or rejected")
    public ResponseEntity<ApplicationResponse> updateStatus(Authentication authentication,
                                                              @PathVariable Long applicationId,
                                                              @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(recruiterService.updateApplicationStatus(authentication.getName(), applicationId, request.getStatus()));
    }
}
