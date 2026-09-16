package com.pms.repository;

import com.pms.model.Drive;
import com.pms.model.Drive.DriveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriveRepository extends JpaRepository<Drive, Long> {
    List<Drive> findByStatus(DriveStatus status);
    List<Drive> findByRecruiterId(Long recruiterId);
    List<Drive> findByRecruiter_User_Email(String email);
}
