package com.pms.repository;

import com.pms.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByDriveId(Long driveId);
    List<Application> findByStudentId(Long studentId);
    Optional<Application> findByStudentIdAndDriveId(Long studentId, Long driveId);
    boolean existsByStudentIdAndDriveId(Long studentId, Long driveId);
}
