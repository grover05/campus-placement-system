package com.pms.service;

import com.pms.model.Drive;
import com.pms.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EligibilityService - core eligibility engine")
class EligibilityServiceTest {

    private EligibilityService eligibilityService;
    private Student student;
    private Drive drive;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();

        student = Student.builder()
                .id(1L)
                .branch("Computer Science")
                .percentage(80.0)
                .backlogs(0)
                .build();

        drive = Drive.builder()
                .id(1L)
                .title("SDE Drive")
                .minPercentage(75.0)
                .maxBacklogs(0)
                .eligibleBranches(Set.of("Computer Science", "Information Technology"))
                .build();
    }

    @Test
    @DisplayName("Student meeting all criteria is eligible")
    void studentMeetingAllCriteria_isEligible() {
        assertTrue(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Student below minimum percentage is not eligible")
    void studentBelowMinPercentage_isNotEligible() {
        student.setPercentage(60.0);
        assertFalse(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Student exceeding max backlogs is not eligible")
    void studentExceedingMaxBacklogs_isNotEligible() {
        student.setBacklogs(2);
        assertFalse(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Student in a non-eligible branch is not eligible")
    void studentInWrongBranch_isNotEligible() {
        student.setBranch("Mechanical Engineering");
        assertFalse(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Drive with no branch restriction accepts every branch")
    void driveWithNoBranchRestriction_acceptsAllBranches() {
        drive.setEligibleBranches(Set.of());
        student.setBranch("Civil Engineering");
        assertTrue(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Branch matching is case-insensitive")
    void branchMatching_isCaseInsensitive() {
        student.setBranch("computer science");
        assertTrue(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Student exactly meeting the minimum percentage is eligible")
    void studentAtExactBoundary_isEligible() {
        student.setPercentage(75.0);
        assertTrue(eligibilityService.isEligible(student, drive));
    }

    @Test
    @DisplayName("Null student or drive is never eligible")
    void nullStudentOrDrive_isNotEligible() {
        assertFalse(eligibilityService.isEligible(null, drive));
        assertFalse(eligibilityService.isEligible(student, null));
    }
}
