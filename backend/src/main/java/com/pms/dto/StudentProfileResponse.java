package com.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
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
