package com.pms.controller;

import com.pms.dto.*;
import com.pms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student", description = "Endpoints for the student role")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    @Operation(summary = "Get my student profile")
    public ResponseEntity<StudentProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me")
    @Operation(summary = "Create or update my student profile")
    public ResponseEntity<StudentProfileResponse> updateProfile(Authentication authentication,
                                                                  @Valid @RequestBody StudentProfileRequest request) {
        return ResponseEntity.ok(studentService.updateProfile(authentication.getName(), request));
    }

    @PostMapping(value = "/me/resume", consumes = "multipart/form-data")
    @Operation(summary = "Upload/replace my resume (PDF, DOC or DOCX, max 5MB)")
    public ResponseEntity<StudentProfileResponse> uploadResume(Authentication authentication,
                                                                 @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentService.uploadResume(authentication.getName(), file));
    }

    @GetMapping("/drives/eligible")
    @Operation(summary = "List open drives I am eligible for")
    public ResponseEntity<List<DriveResponse>> getEligibleDrives(Authentication authentication) {
        return ResponseEntity.ok(studentService.getEligibleDrives(authentication.getName()));
    }

    @PostMapping("/drives/{driveId}/apply")
    @Operation(summary = "Apply to a drive")
    public ResponseEntity<ApplicationResponse> apply(Authentication authentication, @PathVariable Long driveId) {
        return ResponseEntity.ok(studentService.applyToDrive(authentication.getName(), driveId));
    }

    @GetMapping("/applications")
    @Operation(summary = "List my applications and their statuses")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyApplications(authentication.getName()));
    }
}
