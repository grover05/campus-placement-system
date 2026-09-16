package com.pms.dto;

import com.pms.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
