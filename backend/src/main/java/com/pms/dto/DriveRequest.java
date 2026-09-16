package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class DriveRequest {

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

    /** Empty/null means all branches are eligible */
    private Set<String> eligibleBranches;

    @NotNull(message = "Drive date is required")
    @FutureOrPresent(message = "Drive date must be today or in the future")
    private LocalDate driveDate;

    /** Recruiter this drive is tied to. Required when a TPO creates it on a recruiter's behalf. */
    private Long recruiterId;
}
