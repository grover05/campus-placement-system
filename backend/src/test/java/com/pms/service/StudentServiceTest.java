package com.pms.service;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.exception.GlobalExceptionHandler.BadRequestException;
import com.pms.exception.GlobalExceptionHandler.DuplicateResourceException;
import com.pms.model.Application;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive;
import com.pms.model.Drive.DriveStatus;
import com.pms.model.Student;
import com.pms.model.User;
import com.pms.model.User.Role;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService")
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private DriveRepository driveRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private FileStorageService fileStorageService;

    private EligibilityService eligibilityService;
    private StudentService studentService;

    private Student student;
    private Drive openDrive;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();
        studentService = new StudentService(studentRepository, driveRepository, applicationRepository,
                eligibilityService, fileStorageService);

        User user = User.builder().id(1L).email("student@example.com").fullName("Ravi Kumar").role(Role.STUDENT).build();
        student = Student.builder().id(1L).user(user).branch("Computer Science").percentage(85.0).backlogs(0).build();

        openDrive = Drive.builder()
                .id(10L)
                .title("Backend Engineer")
                .companyName("TechCorp")
                .minPercentage(70.0)
                .maxBacklogs(0)
                .eligibleBranches(Set.of("Computer Science"))
                .status(DriveStatus.OPEN)
                .build();
    }

    @Test
    @DisplayName("Applying to an eligible open drive succeeds")
    void applyToDrive_eligibleAndOpen_succeeds() {
        when(studentRepository.findByUser_Email("student@example.com")).thenReturn(Optional.of(student));
        when(driveRepository.findById(10L)).thenReturn(Optional.of(openDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(1L, 10L)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationResponse response = studentService.applyToDrive("student@example.com", 10L);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(applicationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Applying to an ineligible drive throws BadRequestException")
    void applyToDrive_ineligible_throwsException() {
        student.setPercentage(50.0); // below the 70 minimum
        when(studentRepository.findByUser_Email("student@example.com")).thenReturn(Optional.of(student));
        when(driveRepository.findById(10L)).thenReturn(Optional.of(openDrive));

        assertThrows(BadRequestException.class, () -> studentService.applyToDrive("student@example.com", 10L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Applying twice to the same drive throws DuplicateResourceException")
    void applyToDrive_alreadyApplied_throwsException() {
        when(studentRepository.findByUser_Email("student@example.com")).thenReturn(Optional.of(student));
        when(driveRepository.findById(10L)).thenReturn(Optional.of(openDrive));
        when(applicationRepository.existsByStudentIdAndDriveId(1L, 10L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.applyToDrive("student@example.com", 10L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Applying to a closed drive throws BadRequestException")
    void applyToDrive_closedDrive_throwsException() {
        openDrive.setStatus(DriveStatus.CLOSED);
        when(studentRepository.findByUser_Email("student@example.com")).thenReturn(Optional.of(student));
        when(driveRepository.findById(10L)).thenReturn(Optional.of(openDrive));

        assertThrows(BadRequestException.class, () -> studentService.applyToDrive("student@example.com", 10L));
    }

    @Test
    @DisplayName("Eligible drives list only returns drives that match the student's profile")
    void getEligibleDrives_filtersOutIneligibleDrives() {
        Drive ineligibleDrive = Drive.builder()
                .id(11L).title("Mechanical Role").companyName("HeavyCorp")
                .minPercentage(90.0).maxBacklogs(0)
                .eligibleBranches(Set.of("Mechanical Engineering"))
                .status(DriveStatus.OPEN)
                .build();

        when(studentRepository.findByUser_Email("student@example.com")).thenReturn(Optional.of(student));
        when(driveRepository.findByStatus(DriveStatus.OPEN)).thenReturn(List.of(openDrive, ineligibleDrive));

        List<DriveResponse> eligible = studentService.getEligibleDrives("student@example.com");

        assertEquals(1, eligible.size());
        assertEquals(openDrive.getId(), eligible.get(0).getId());
    }
}
