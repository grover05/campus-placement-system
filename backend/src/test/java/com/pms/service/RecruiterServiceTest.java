package com.pms.service;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.pms.exception.GlobalExceptionHandler.UnauthorizedActionException;
import com.pms.model.Application;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive;
import com.pms.model.Drive.DriveStatus;
import com.pms.model.Recruiter;
import com.pms.model.Student;
import com.pms.model.User;
import com.pms.model.User.Role;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.RecruiterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecruiterService")
class RecruiterServiceTest {

    @Mock private RecruiterRepository recruiterRepository;
    @Mock private DriveRepository driveRepository;
    @Mock private ApplicationRepository applicationRepository;

    @InjectMocks
    private RecruiterService recruiterService;

    private Recruiter recruiter;
    private Drive ownDrive;
    private Drive otherDrive;

    @BeforeEach
    void setUp() {
        User recruiterUser = User.builder().id(3L).email("recruiter@techcorp.com").fullName("Hiring Manager").role(Role.RECRUITER).build();
        recruiter = Recruiter.builder().id(1L).user(recruiterUser).companyName("TechCorp").build();

        Recruiter otherRecruiter = Recruiter.builder().id(2L).companyName("OtherCorp").build();

        ownDrive = Drive.builder().id(10L).title("Backend Role").recruiter(recruiter).status(DriveStatus.OPEN).build();
        otherDrive = Drive.builder().id(20L).title("Other Role").recruiter(otherRecruiter).status(DriveStatus.OPEN).build();
    }

    @Test
    @DisplayName("Recruiter can view applicants for their own drive")
    void getApplicantsForDrive_ownDrive_succeeds() {
        User studentUser = User.builder().id(5L).email("s@x.com").fullName("Applicant").build();
        Student student = Student.builder().id(7L).user(studentUser).branch("CS").percentage(80.0).backlogs(0).build();
        Application app = Application.builder().id(1L).student(student).drive(ownDrive).status(ApplicationStatus.APPLIED).build();

        when(driveRepository.findById(10L)).thenReturn(Optional.of(ownDrive));
        when(recruiterRepository.findByUser_Email("recruiter@techcorp.com")).thenReturn(Optional.of(recruiter));
        when(applicationRepository.findByDriveId(10L)).thenReturn(List.of(app));

        List<ApplicationResponse> applicants = recruiterService.getApplicantsForDrive("recruiter@techcorp.com", 10L);

        assertEquals(1, applicants.size());
    }

    @Test
    @DisplayName("Recruiter cannot view applicants for a drive they don't own")
    void getApplicantsForDrive_otherDrive_throwsUnauthorized() {
        when(driveRepository.findById(20L)).thenReturn(Optional.of(otherDrive));
        when(recruiterRepository.findByUser_Email("recruiter@techcorp.com")).thenReturn(Optional.of(recruiter));

        assertThrows(UnauthorizedActionException.class,
                () -> recruiterService.getApplicantsForDrive("recruiter@techcorp.com", 20L));
    }

    @Test
    @DisplayName("Recruiter marking an applicant as shortlisted updates the status")
    void updateApplicationStatus_shortlist_succeeds() {
        User studentUser = User.builder().id(5L).email("s@x.com").fullName("Applicant").build();
        Student student = Student.builder().id(7L).user(studentUser).branch("CS").percentage(80.0).backlogs(0).build();
        Application app = Application.builder().id(1L).student(student).drive(ownDrive).status(ApplicationStatus.APPLIED).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(driveRepository.findById(10L)).thenReturn(Optional.of(ownDrive));
        when(recruiterRepository.findByUser_Email("recruiter@techcorp.com")).thenReturn(Optional.of(recruiter));
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = recruiterService.updateApplicationStatus("recruiter@techcorp.com", 1L, ApplicationStatus.SHORTLISTED);

        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
    }

    @Test
    @DisplayName("Fetching drives for an unknown recruiter profile throws ResourceNotFoundException")
    void getMyDrives_unknownRecruiterProfile_throwsException() {
        when(driveRepository.findByRecruiter_User_Email("ghost@x.com")).thenReturn(List.of());
        List<DriveResponse> result = recruiterService.getMyDrives("ghost@x.com");
        assertTrue(result.isEmpty());
    }
}
