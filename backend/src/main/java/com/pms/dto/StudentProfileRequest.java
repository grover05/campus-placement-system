package com.pms.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StudentProfileRequest {

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
