package com.pms.dto;

import com.pms.model.DriveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveResponse {
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
