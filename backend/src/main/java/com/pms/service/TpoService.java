package com.pms.service;

import com.pms.dto.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.model.*;
import com.pms.repository.ApplicationRepository;
import com.pms.repository.DriveRepository;
import com.pms.repository.RecruiterRepository;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TpoService {

    private final DriveRepository driveRepository;
    private final RecruiterRepository recruiterRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public DriveResponse createDrive(String tpoEmail, DriveRequest request) {
        User tpo = userRepository.findByEmail(tpoEmail)
                .orElseThrow(() -> new ResourceNotFoundException("TPO user not found"));

        Recruiter recruiter = null;
        if (request.getRecruiterId() != null) {
            recruiter = recruiterRepository.findById(request.getRecruiterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with id " + request.getRecruiterId()));
        }

        Drive drive = Drive.builder()
                .title(request.getTitle())
                .companyName(request.getCompanyName())
                .description(request.getDescription())
                .minPercentage(request.getMinPercentage())
                .maxBacklogs(request.getMaxBacklogs())
                .eligibleBranches(request.getEligibleBranches())
                .driveDate(request.getDriveDate())
                .recruiter(recruiter)
                .postedBy(tpo)
                .status(DriveStatus.OPEN)
                .build();

        drive = driveRepository.save(drive);
        return toDriveResponse(drive);
    }

    public List<DriveResponse> getAllDrives() {
        return driveRepository.findAll().stream().map(this::toDriveResponse).toList();
    }

    @Transactional
    public DriveResponse updateDriveStatus(Long driveId, DriveStatus status) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id " + driveId));
        drive.setStatus(status);
        return toDriveResponse(driveRepository.save(drive));
    }

    public List<ApplicationResponse> getApplicationsForDrive(Long driveId) {
        if (!driveRepository.existsById(driveId)) {
            throw new ResourceNotFoundException("Drive not found with id " + driveId);
        }
        return applicationRepository.findByDriveId(driveId).stream()
                .map(this::toApplicationResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id " + applicationId));
        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        return toApplicationResponse(applicationRepository.save(application));
    }

    /**
     * Export the shortlisted students for a drive as a CSV byte stream,
     * ready to be returned to the browser as a file download.
     */
    public byte[] exportShortlist(Long driveId) {
        Drive drive = driveRepository.findById(driveId)
                .orElseThrow(() -> new ResourceNotFoundException("Drive not found with id " + driveId));

        List<Application> shortlisted = applicationRepository.findByDriveId(driveId).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED)
                .toList();

        if (shortlisted.isEmpty()) {
            throw new BadRequestException("No shortlisted students for drive: " + drive.getTitle());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            writer.println("Name,Email,Branch,Percentage,Backlogs,ResumeURL");
            for (Application app : shortlisted) {
                Student s = app.getStudent();
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        escape(s.getUser().getFullName()),
                        escape(s.getUser().getEmail()),
                        escape(s.getBranch()),
                        s.getPercentage(),
                        s.getBacklogs(),
                        escape(s.getResumeUrl()));
            }
        }
        return out.toByteArray();
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.contains(",") ? "\"" + value + "\"" : value;
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
