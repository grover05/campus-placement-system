package com.pms.dto;

import com.pms.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
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
