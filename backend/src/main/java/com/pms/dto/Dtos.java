package com.pms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive.DriveStatus;
import com.pms.model.User.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Consolidated container for all request and response DTOs.
 */
public final class Dtos {

    private Dtos() {}

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotNull(message = "Role is required")
        private Role role;

        private String companyName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
        private Role role;
    }

    @Data
    public static class StudentProfileRequest {
        @NotBlank(message = "Branch is required")
        private String branch;

        @NotNull(message = "Percentage is required")
        @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
        @DecimalMax(value = "100.0", message = "Percentage cannot exceed 100")
        private Double percentage;

        @NotNull(message = "Backlogs count is required")
        @Min(value = 0, message = "Backlogs cannot be negative")
        private Integer backlogs;

        @NotNull(message = "Graduation year is required")
        private Integer graduationYear;

        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProfileResponse {
        private Long id;
        private String fullName;
        private String email;
        private String branch;
        private Double percentage;
        private Integer backlogs;
        private String resumeUrl;
        private Integer graduationYear;
        private String phone;
    }

    @Data
    public static class DriveRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Company name is required")
        private String companyName;

        private String description;

        @NotNull(message = "Minimum percentage criterion is required")
        @DecimalMin(value = "0.0", message = "Minimum percentage cannot be negative")
        @DecimalMax(value = "100.0", message = "Minimum percentage cannot exceed 100")
        private Double minPercentage;

        @NotNull(message = "Max backlogs criterion is required")
        @Min(value = 0, message = "Max backlogs cannot be negative")
        private Integer maxBacklogs;

        private Set<String> eligibleBranches;

        @NotNull(message = "Drive date is required")
        @FutureOrPresent(message = "Drive date must be today or in the future")
        private LocalDate driveDate;

        private Long recruiterId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriveResponse {
        private Long id;
        private String title;
        private String companyName;
        private String description;
        private Double minPercentage;
        private Integer maxBacklogs;
        private Set<String> eligibleBranches;
        private LocalDate driveDate;
        private DriveStatus status;
        private Long recruiterId;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationResponse {
        private Long id;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private String branch;
        private Double percentage;
        private Integer backlogs;
        private String resumeUrl;
        private Long driveId;
        private String driveTitle;
        private String companyName;
        private ApplicationStatus status;
        private LocalDateTime appliedAt;
    }

    @Data
    public static class ApplicationStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private ApplicationStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> details;
    }
}
