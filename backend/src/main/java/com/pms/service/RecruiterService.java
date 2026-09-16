package com.pms.service;

import com.pms.dto.Dtos.ApplicationResponse;
import com.pms.dto.Dtos.DriveResponse;
import com.pms.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.pms.exception.GlobalExceptionHandler.UnauthorizedActionException;
import com.pms.model.Application;
import com.pms.model.Application.ApplicationStatus;
import com.pms.model.Drive;
import com.pms.model.Recruiter;
import com.pms.model.Student;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.RecruiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;
    private final DriveRepository driveRepository;
    private final ApplicationRepository applicationRepository;

    public Recruiter getRecruiterByEmail(String email) {
        return recruiterRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found for " + email));
    }

    public List<DriveResponse> getMyDrives(String email) {
        return driveRepository.findByRecruiter_User_Email(email).stream()
                .map(this::toDriveResponse)
                .toList();
    }

    public List<ApplicationResponse> getApplicantsForDrive(String email, Long driveId) {
        Drive drive = getOwnedDrive(email, driveId);
        return applicationRepository.findByDriveId(drive.getId()).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(String email, Long applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + applicationId));

        // A recruiter may only act on applications belonging to their own drives.
        getOwnedDrive(email, application.getDrive().getId());

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        return toApplicationResponse(applicationRepository.save(application));
    }

    private Drive getOwnedDrive(String email, Long driveId) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id " + driveId));

        Recruiter recruiter = getRecruiterByEmail(email);

        if (drive.getRecruiter() == null || !drive.getRecruiter().getId().equals(recruiter.getId())) {
            throw new UnauthorizedActionException("You do not have access to this drive");
        }
        return drive;
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
