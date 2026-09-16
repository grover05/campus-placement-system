package com.pms.service;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.DriveRequest;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.exception.GlobalExceptionHandler.BadRequestException;
import com.pms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.pms.model.Application;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive;
import com.pms.model.Drive.DriveStatus;
import com.pms.model.User;
import com.pms.model.User.Role;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.RecruiterRepository;
import com.pms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TpoService")
class TpoServiceTest {

    @Mock private DriveRepository driveRepository;
    @Mock private RecruiterRepository recruiterRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks
    private TpoService tpoService;

    private User tpoUser;
    private Drive drive;

    @BeforeEach
    void setUp() {
        tpoUser = User.builder().id(1L).email("tpo@college.edu").fullName("Placement Officer").role(Role.TPO).build();
        drive = Drive.builder().id(5L).title("SDE Drive").companyName("TechCorp")
                .minPercentage(70.0).maxBacklogs(0).eligibleBranches(Set.of("Computer Science"))
                .driveDate(LocalDate.now().plusDays(10)).status(DriveStatus.OPEN).postedBy(tpoUser).build();
    }

    @Test
    @DisplayName("Creating a drive without a recruiter link succeeds")
    void createDrive_withoutRecruiter_succeeds() {
        DriveRequest request = new DriveRequest();
        request.setTitle("SDE Drive");
        request.setCompanyName("TechCorp");
        request.setMinPercentage(70.0);
        request.setMaxBacklogs(0);
        request.setEligibleBranches(Set.of("Computer Science"));
        request.setDriveDate(LocalDate.now().plusDays(10));

        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(tpoUser));
        when(driveRepository.save(any(Drive.class))).thenAnswer(inv -> {
            Drive d = inv.getArgument(0);
            d.setId(5L);
            return d;
        });

        DriveResponse response = tpoService.createDrive("tpo@college.edu", request);

        assertNotNull(response);
        assertEquals("SDE Drive", response.getTitle());
        assertEquals(DriveStatus.OPEN, response.getStatus());
        verify(recruiterRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Creating a drive with an unknown recruiter id throws ResourceNotFoundException")
    void createDrive_unknownRecruiter_throwsException() {
        DriveRequest request = new DriveRequest();
        request.setTitle("SDE Drive");
        request.setCompanyName("TechCorp");
        request.setMinPercentage(70.0);
        request.setMaxBacklogs(0);
        request.setDriveDate(LocalDate.now().plusDays(10));
        request.setRecruiterId(99L);

        when(userRepository.findByEmail("tpo@college.edu")).thenReturn(Optional.of(tpoUser));
        when(recruiterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tpoService.createDrive("tpo@college.edu", request));
    }

    @Test
    @DisplayName("Updating application status persists the new status")
    void updateApplicationStatus_updatesStatus() {
        User studentUser = User.builder().id(2L).email("s@x.com").fullName("Student One").build();
        com.pms.model.Student student = com.pms.model.Student.builder().id(1L).user(studentUser).branch("CS").percentage(80.0).backlogs(0).build();
        Application application = Application.builder().id(100L).student(student).drive(drive).status(ApplicationStatus.APPLIED).build();

        when(applicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = tpoService.updateApplicationStatus(100L, ApplicationStatus.SHORTLISTED);

        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
    }

    @Test
    @DisplayName("Exporting a shortlist with no shortlisted students throws BadRequestException")
    void exportShortlist_noShortlistedStudents_throwsException() {
        when(driveRepository.findById(5L)).thenReturn(Optional.of(drive));
        when(applicationRepository.findByDriveId(5L)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> tpoService.exportShortlist(5L));
    }

    @Test
    @DisplayName("Exporting a shortlist for an unknown drive throws ResourceNotFoundException")
    void exportShortlist_unknownDrive_throwsException() {
        when(driveRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tpoService.exportShortlist(999L));
    }
}
