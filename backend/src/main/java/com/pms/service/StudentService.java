package com.pms.service;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.dto.Dtos.StudentProfileRequest;
import com.pms.dto.Dtos.StudentProfileResponse;
import com.pms.exception.GlobalExceptionHandler.BadRequestException;
import com.pms.exception.GlobalExceptionHandler.DuplicateResourceException;
import com.pms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.pms.model.Application;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive;
import com.pms.model.Drive.DriveStatus;
import com.pms.model.Student;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;
    private final EligibilityService eligibilityService;
    private final FileStorageService fileStorageService;

    public Student getStudentByEmail(String email) {
        return studentRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for " + email));
    }

    public StudentProfileResponse getMyProfile(String email) {
        return toResponse(getStudentByEmail(email));
    }

    @Transactional
    public StudentProfileResponse updateProfile(String email, StudentProfileRequest request) {
        Student student = getStudentByEmail(email);
        student.setBranch(request.getBranch());
        student.setPercentage(request.getPercentage());
        student.setBacklogs(request.getBacklogs());
        student.setGraduationYear(request.getGraduationYear());
        student.setPhone(request.getPhone());
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentProfileResponse uploadResume(String email, MultipartFile file) {
        Student student = getStudentByEmail(email);
        String url = fileStorageService.storeResume(file, student.getId());
        student.setResumeUrl(url);
        return toResponse(studentRepository.save(student));
    }

    public List<DriveResponse> getEligibleDrives(String email) {
        Student student = getStudentByEmail(email);
        List<Drive> openDrives = driveRepository.findByStatus(DriveStatus.OPEN);

        return openDrives.stream()
                .filter(drive -> eligibilityService.isEligible(student, drive))
                .map(this::toDriveResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse applyToDrive(String email, Long driveId) {
        Student student = getStudentByEmail(email);
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id " + driveId));

        if (drive.getStatus() != DriveStatus.OPEN) {
            throw new BadRequestException("This drive is no longer accepting applications");
        }

        if (!eligibilityService.isEligible(student, drive)) {
            throw new BadRequestException("You do not meet the eligibility criteria for this drive");
        }

        if (applicationRepository.existsByStudentIdAndDriveId(student.getId(), driveId)) {
            throw new DuplicateResourceException("You have already applied to this drive");
        }

        Application application = Application.builder()
                .student(student)
                .drive(drive)
                .status(ApplicationStatus.APPLIED)
                .build();

        application = applicationRepository.save(application);
        return toApplicationResponse(application);
    }

    public List<ApplicationResponse> getMyApplications(String email) {
        Student student = getStudentByEmail(email);
        return applicationRepository.findByStudentId(student.getId()).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    private StudentProfileResponse toResponse(Student student) {
        return StudentProfileResponse.builder()
                .id(student.getId())
                .fullName(student.getUser().getFullName())
                .email(student.getUser().getEmail())
                .branch(student.getBranch())
                .percentage(student.getPercentage())
                .backlogs(student.getBacklogs())
                .resumeUrl(student.getResumeUrl())
                .graduationYear(student.getGraduationYear())
                .phone(student.getPhone())
                .build();
    }

    private DriveResponse toDriveResponse(Drive drive) {
        return DriveResponse.builder()
                .id(drive.getId())
                .title(drive.getTitle())
                .companyName(drive.getCompanyName())
                .description(drive.getDescription())
                .minPercentage(drive.getMinPercentage())
                .maxBacklogs(drive.getMaxBacklogs())
                .eligibleBranches(drive.getEligibleBranches())
                .driveDate(drive.getDriveDate())
                .status(drive.getStatus())
                .recruiterId(drive.getRecruiter() != null ? drive.getRecruiter().getId() : null)
                .createdAt(drive.getCreatedAt())
                .build();
    }

    private ApplicationResponse toApplicationResponse(Application application) {
        Student s = application.getStudent();
        Drive d = application.getDrive();
        return ApplicationResponse.builder()
                .id(application.getId())
                .studentId(s.getId())
                .studentName(s.getUser().getFullName())
                .studentEmail(s.getUser().getEmail())
                .branch(s.getBranch())
                .percentage(s.getPercentage())
                .backlogs(s.getBacklogs())
                .resumeUrl(s.getResumeUrl())
                .driveId(d.getId())
                .driveTitle(d.getTitle())
                .companyName(d.getCompanyName())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}
